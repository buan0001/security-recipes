package dat3.recipe.api;

import dat3.recipe.entity.Category;
import dat3.recipe.entity.Recipe;
import dat3.recipe.repository.CategoryRepository;
import dat3.recipe.service.CategoryService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    CategoryService categoryService;


    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<String> getAllCategories() {
        System.out.println("GETTING CATEGORIES@@@@@@@@@@@@");
        return categoryService.getAllCategories();
    }

//    @GetMapping
//    public ResponseEntity<List<String>> getAllCategories() {
//        List<String> categories = categoryService.getAllCategories();
//
//        CacheControl cacheControl = CacheControl.maxAge(20, TimeUnit.SECONDS).cachePublic();
//
//        return ResponseEntity.ok()
//                .cacheControl(cacheControl)
//                .body(categories);
//    }


    @PostMapping
    public List<String> createCategory(@RequestBody Category newCategory){
        return categoryService.save(newCategory);
    }
}
