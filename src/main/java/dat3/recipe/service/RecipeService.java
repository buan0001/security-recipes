package dat3.recipe.service;

import dat3.recipe.dto.RecipeDto;
import dat3.recipe.entity.Category;
import dat3.recipe.entity.Recipe;
import dat3.recipe.repository.CategoryRepository;
import dat3.recipe.repository.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Service
public class RecipeService {

    private RecipeRepository recipeRepository;
    private CategoryRepository categoryRepository;

    public RecipeService(RecipeRepository recipeRepository, CategoryRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
    }


    public List<RecipeDto> getAllRecipes(String category) {
        List<Recipe> recipes = category == null ? recipeRepository.findAll() : recipeRepository.findByCategoryName(category);
        List<RecipeDto> recipeResponses = recipes.stream().map((r) -> new RecipeDto(r,false)).toList();
        return recipeResponses;
    }

    public RecipeDto getRecipeById(int idInt) {
        Recipe recipe = recipeRepository.findById(idInt).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        return new RecipeDto(recipe,false);
    }

    public RecipeDto addRecipe(RecipeDto request, JwtAuthenticationToken p) {
        System.out.println("@@@@@@@@ " + p.getName() + " @@@@@@@@@@@@@@@@@@");

        if (request.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot provide the id for a new recipe");
        }
        Category category = categoryRepository.findByName(request.getCategory()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Only existing categories are allowed"));
        Recipe newRecipe = new Recipe();
        updateRecipe(newRecipe, request, category);
        newRecipe.setOwner(p.getName());
        recipeRepository.save(newRecipe);
        return new RecipeDto(newRecipe,false);

    }
    private void updateRecipe(Recipe original, RecipeDto r, Category category) {
        original.setName(r.getName());
        original.setInstructions(r.getInstructions());
        original.setIngredients(r.getIngredients());
        original.setThumb(r.getThumb());
        original.setYouTube(r.getYouTube());
        original.setSource(r.getSource());
        original.setCategory(category);
    }

    public RecipeDto editRecipe(RecipeDto request, int id,JwtAuthenticationToken p) {
        if (request.getId() != id) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot change the id of an existing recipe");
        }
        Category category = categoryRepository.findByName(request.getCategory()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Only existing categories are allowed"));

        Recipe recipeToEdit = recipeRepository.findById(id).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        if (recipeToEdit.getOwner() != null && !recipeToEdit.getOwner().equals(p.getName())) {throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only the recipe's owner can edit the recipe");}
        updateRecipe(recipeToEdit,request, category);
        recipeRepository.save(recipeToEdit);
        return new RecipeDto(recipeToEdit,false);
    }

    public ResponseEntity deleteRecipe(int id,JwtAuthenticationToken p) {
        System.out.println("@@@@@@@@@ " + p.getAuthorities() + " @@@@@@@@");
        System.out.println("@@@@@@@@@ " + p.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")) + " @@@@@@@@");
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        if (!recipe.getOwner().equals(p.getName()) && !p.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")) ) {throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only the recipe's owner can delete the recipe");}
        recipeRepository.delete(recipe);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

//    private static void checkRoles(String owner) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
//        List<String> roles = authorities.stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());
//        boolean isAdmin = roles.contains("ADMIN");
//        String name = auth.getName();
//        if(!isAdmin && !name.equals(owner)){
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You may only edit/delete your own recipes");
//        }
//    }



}
