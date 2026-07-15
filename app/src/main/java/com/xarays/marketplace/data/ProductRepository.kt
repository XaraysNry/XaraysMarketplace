package com.xarays.marketplace.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import com.xarays.marketplace.model.Product
import com.xarays.marketplace.model.ProductType
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    suspend fun getAllProducts(): List<Product> {
        return try {
            val snapshot = productsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Product(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        game = doc.getString("game") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        description = doc.getString("description") ?: "",
                        imageRes = getImageResource(doc.getString("game") ?: ""),
                        type = ProductType.valueOf(doc.getString("type") ?: ProductType.ACCOUNT.name),
                        sellerId = doc.getString("sellerId") ?: "admin",
                        stock = doc.getLong("stock")?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun observeAllProducts(
        onChange: (List<Product>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return productsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val products = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    try {
                        Product(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            game = doc.getString("game") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            description = doc.getString("description") ?: "",
                            imageRes = getImageResource(doc.getString("game") ?: ""),
                            type = ProductType.valueOf(doc.getString("type") ?: ProductType.ACCOUNT.name),
                            sellerId = doc.getString("sellerId") ?: "admin",
                            stock = doc.getLong("stock")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                onChange(products)
            }
    }

    suspend fun getProductById(productId: String): Product? {
        return try {
            val doc = productsCollection.document(productId).get().await()
            if (doc.exists()) {
                Product(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    game = doc.getString("game") ?: "",
                    price = doc.getDouble("price") ?: 0.0,
                    description = doc.getString("description") ?: "",
                    imageRes = getImageResource(doc.getString("game") ?: ""),
                    type = ProductType.valueOf(doc.getString("type") ?: ProductType.ACCOUNT.name),
                    sellerId = doc.getString("sellerId") ?: "admin",
                    stock = doc.getLong("stock")?.toInt() ?: 0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addProduct(product: Product): Result<String> {
        return try {
            val docRef = productsCollection.document()
            val productData = hashMapOf(
                "title" to product.title,
                "game" to product.game,
                "price" to product.price,
                "description" to product.description,
                "type" to product.type.name,
                "sellerId" to product.sellerId,
                "stock" to product.stock,
                "createdAt" to System.currentTimeMillis()
            )
            docRef.set(productData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(productId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            productsCollection.document(productId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getImageResource(gameName: String): Int {
        return when (gameName.lowercase()) {
            "mobile legends" -> com.xarays.marketplace.R.drawable.game_moba
            "free fire" -> com.xarays.marketplace.R.drawable.game_battle_royale
            "pubg" -> com.xarays.marketplace.R.drawable.game_battle_royale_pubg
            "genshin impact" -> com.xarays.marketplace.R.drawable.game_rpg
            else -> com.xarays.marketplace.R.drawable.game_moba
        }
    }
}
