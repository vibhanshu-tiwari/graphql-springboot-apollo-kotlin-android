package com.vibh.graphql_spring_api.controller;

import com.vibh.graphql_spring_api.exception.ProductNotFoundException;
import com.vibh.graphql_spring_api.model.Product;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class ProductController {

    private final List<Product> productList = new ArrayList<>(List.of(
            new Product("1", "Keyboard", 1200.0),
            new Product("2", "Mouse", 700.0),
            new Product("3", "Monitor", 15000.0)
    ));

    long maxId = productList.stream()
            .mapToLong(product -> Long.parseLong(product.getId()))
            .max()
            .orElse(0L);

    private final AtomicLong productIdGenerator =
            new AtomicLong(maxId);

   /* private final Sinks.Many<Product> productSink =
            Sinks.many().multicast().onBackpressureBuffer();*/

    private final Sinks.Many<Product> productSink =
            Sinks.many()
                    .multicast()
                    .onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    //@QueryMapping("products")
    @QueryMapping
    public List<Product> products() {
        return productList;
    }

    @QueryMapping
    public Product product(@Argument String id) {
        return productList.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                //.orElseThrow(() -> new RuntimeException("Product Not Found"));
                //.orElse(null);
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    @MutationMapping
    public Product addProduct(@Argument String name, @Argument double price) {
        String id = String.valueOf(productIdGenerator.incrementAndGet());
        Product product = new Product(id, name, price);
        productList.add(product);
       // productSink.tryEmitNext(product);
        Sinks.EmitResult result = productSink.tryEmitNext(product);

        if (result.isFailure()) {
            System.out.println("Product event emission failed: " + result);
        }

        return product;
    }

    @MutationMapping
    public Product updateProduct(@Argument String id, @Argument String name, @Argument double price) {
        return productList.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                .map(product ->{
                    product.setName(name);
                    product.setPrice(price);
                    return product;
                })
                .orElse(null);
    }

    @MutationMapping
    public boolean deleteProduct(@Argument String id) {
        return productList.removeIf(product -> product.getId().equals(id));
    }

    @SubscriptionMapping
    public Flux<Product> productAdded() {
        System.out.println("GraphQL subscription requested");

        return productSink.asFlux()
                .doOnSubscribe(subscription ->
                        System.out.println("Product subscriber registered"))
                .doOnCancel(() ->
                        System.out.println("Product subscriber cancelled"))
                .doOnComplete(() ->
                        System.out.println("Product subscription completed"))
                .doOnError(error ->
                        System.out.println("Product subscription error: " + error));
    }
}
