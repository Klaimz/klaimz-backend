package com.klaimz.api;


import com.klaimz.model.Product;
import com.klaimz.model.http.MessageBean;
import com.klaimz.service.ProductService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

import static com.klaimz.util.HttpUtils.*;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Controller("/product")
@Secured(IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
public class ProductController {


    @Inject
    private ProductService productService;

    @Get("/{id}")
    public HttpResponse<MessageBean> getProductById(String id) {
        var product = productService.getProductById(id);
        return success(product, "Product found");
    }

    @Get("/all")
    public HttpResponse<MessageBean> getAllProducts() {
        var products = productService.getAllProducts();
        return success(products, "All products");
    }

    @Post
    public HttpResponse<MessageBean> createProduct(@Body Product product) {
        product.setId(null); // ensure id is not set, it will be generated
        var newProduct = productService.createProduct(product);
        return success(newProduct, "Product created successfully");
    }
}
