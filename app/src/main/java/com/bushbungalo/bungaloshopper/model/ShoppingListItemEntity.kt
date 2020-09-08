package com.bushbungalo.bungaloshopper.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_list")
data class ShoppingListItemEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Int,
    @ColumnInfo(name = "list_id")
    var shoppingListId: String,
    @ColumnInfo(name = "product_name")
    var productName: String,
    @ColumnInfo(name = "product_category")
    var productCategory: String,
    @ColumnInfo(name = "product_quantity")
    var productQuantity: Int,
    @ColumnInfo(name = "product_unit_price")
    var unitPrice: Double,
    @ColumnInfo(name = "shopping_date")
    var shoppingDate: Long
)
