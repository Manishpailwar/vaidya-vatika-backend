package com.vaidyavatika.config;

import com.vaidyavatika.model.Product;
import com.vaidyavatika.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DataSeeder runs once when the app starts.
 * It seeds the database with sample products IF the products table is empty.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            log.info("🌿 Seeding sample products into database...");
            seedProducts();
            log.info("✅ Sample products seeded successfully!");
        } else {
            log.info("📦 Products already exist in database. Skipping seed.");
        }
    }

    private void seedProducts() {
        List<Product> products = List.of(
            Product.builder()
                .name("Multigrain Daliya")
                .description("A nourishing blend of 7 whole grains — wheat, oats, barley, ragi, bajra, jowar & maize. Rich in fibre, protein and complex carbohydrates. Perfect for a healthy breakfast.")
                .price(199.0)
                .imageUrl("https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=400&q=80")
                .category("Grains")
                .stock(50)
                .badge("Bestseller")
                .isActive(true)
                .build(),

            Product.builder()
                .name("Raw Forest Honey")
                .description("Unprocessed, unfiltered wild forest honey harvested by tribal communities. Packed with natural enzymes, antioxidants & healing properties. No heating, no filtering.")
                .price(349.0)
                .imageUrl("https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=400&q=80")
                .category("Honey")
                .stock(30)
                .badge("Pure")
                .isActive(true)
                .build(),

            Product.builder()
                .name("Ashwagandha Powder")
                .description("Premium sun-dried Ashwagandha root powder from certified organic farms in Rajasthan. Boosts immunity, reduces stress and enhances vitality naturally.")
                .price(249.0)
                .imageUrl("https://images.unsplash.com/photo-1611073615830-9b11c37a1f9a?w=400&q=80")
                .category("Herbs")
                .stock(40)
                .badge("Organic")
                .isActive(true)
                .build(),

            Product.builder()
                .name("Cold Pressed Coconut Oil")
                .description("Virgin coconut oil extracted through cold-press method preserving all nutrients and aroma. Ideal for cooking, hair care and skin nourishment.")
                .price(299.0)
                .imageUrl("https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400&q=80")
                .category("Oils")
                .stock(25)
                .badge("Virgin")
                .isActive(true)
                .build(),

            Product.builder()
                .name("Moringa Leaf Powder")
                .description("Nutrient-dense superfood from the Miracle Tree. Contains 90+ nutrients, 46 antioxidants and all essential amino acids. Great addition to smoothies.")
                .price(199.0)
                .imageUrl("https://images.unsplash.com/photo-1556909172-54557c7e4fb7?w=400&q=80")
                .category("Herbs")
                .stock(60)
                .badge("Superfood")
                .isActive(true)
                .build(),

            Product.builder()
                .name("Mixed Grain Flour")
                .description("Wholesome blend of 5 ancient grains ground fresh. High protein, high fibre alternative to refined flour. Perfect for rotis, parathas and chapatis.")
                .price(179.0)
                .imageUrl("https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400&q=80")
                .category("Grains")
                .stock(45)
                .badge("Fresh Ground")
                .isActive(true)
                .build(),

            Product.builder()
                .name("Tulsi Honey")
                .description("Raw honey infused with fresh Tulsi (Holy Basil) leaves. A powerful combination for respiratory health, immunity and digestion. Limited seasonal production.")
                .price(389.0)
                .imageUrl("https://images.unsplash.com/photo-1558642452-9d2a7deb7f62?w=400&q=80")
                .category("Honey")
                .stock(20)
                .badge("Limited")
                .isActive(true)
                .build(),

            Product.builder()
                .name("Triphala Churna")
                .description("Classic Ayurvedic formulation of Amalaki, Bibhitaki and Haritaki in equal proportions. The ultimate digestive tonic and natural detoxifier used for centuries.")
                .price(219.0)
                .imageUrl("https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400&q=80")
                .category("Herbs")
                .stock(35)
                .badge("Classical")
                .isActive(true)
                .build()
        );

        productRepository.saveAll(products);
    }
}
