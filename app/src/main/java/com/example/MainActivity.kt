package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Define Premium Luxury Theme Colors directly in code
private val VelvetRose = Color(0xFF9D1F4F)
private val BlushPink = Color(0xFFF7CAD0)
private val DeepCharcoal = Color(14, 10, 12)
private val DeepCrimsonSurface = Color(0xFF261A1E)
private val AccentGold = Color(0xFFD4AF37)
private val CardBackground = Color(0xFF332328)

data class Product(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val price: Double,
    val category: String,
    val stock: Int,
    val icon: String
)

data class CartItem(
    val product: Product,
    var quantity: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeautyPosApp()
        }
    }
}

@Composable
fun BeautyPosApp() {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("POS") } // POS, Inventory, Reports

    // Mock Database State
    val products = remember {
        mutableStateListOf(
            Product("1", "أحمر شفاه كلاسيكي مرطب", "Classic Moisturizing Lipstick", 120.0, "Makeup", 15, "💄"),
            Product("2", "عطر الياسمين الفاخر 100 مل", "Luxury Jasmine Perfume 100ml", 450.0, "Perfumes", 4, "✨"),
            Product("3", "كريم مرطب واقي من الشمس", "Moisturizing Sunscreen Cream", 180.0, "Skincare", 22, "🧴"),
            Product("4", "ماسكارا تطويل وتكثيف مدمجة", "Volume & Length Mascara", 95.0, "Makeup", 0, "👁️"),
            Product("5", "عطر العود والمسك الملكي", "Royal Oud & Musk Fragrance", 650.0, "Perfumes", 8, "👑"),
            Product("6", "سيروم الهيالورونيك اسيد للوجه", "Hyaluronic Acid Facial Serum", 210.0, "Skincare", 12, "🧪")
        )
    }

    val cart = remember { mutableStateListOf<CartItem>() }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var customDiscountInput by remember { mutableStateOf("") }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var completedSaleReceipt by remember { mutableStateOf<List<CartItem>?>(null) }
    var totalReceiptAmount by remember { mutableStateOf(0.0) }

    // Helpers
    val filteredProducts = products.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
                (it.nameAr.contains(searchQuery, ignoreCase = true) || it.nameEn.contains(searchQuery, ignoreCase = true))
    }

    val cartSubtotal = cart.sumOf { it.product.price * it.quantity }
    val appliedDiscount = customDiscountInput.toDoubleOrNull() ?: 0.0
    val cartTotal = (cartSubtotal - appliedDiscount).coerceAtLeast(0.0)

    fun addToCart(product: Product) {
        if (product.stock <= 0) {
            Toast.makeText(context, "هذا المنتج غير متوفر في المخزون حالياً", Toast.LENGTH_SHORT).show()
            return
        }
        val existing = cart.find { it.product.id == product.id }
        if (existing != null) {
            if (existing.quantity >= product.stock) {
                Toast.makeText(context, "تم الوصول للحد الأقصى للمتوفر بالمخزن", Toast.LENGTH_SHORT).show()
                return
            }
            existing.quantity++
            // Force state recomposition for deep properties
            val idx = cart.indexOf(existing)
            cart[idx] = existing.copy(quantity = existing.quantity)
        } else {
            cart.add(CartItem(product, 1))
        }
    }

    fun decreaseQuantity(item: CartItem) {
        if (item.quantity > 1) {
            item.quantity--
            val idx = cart.indexOf(item)
            cart[idx] = item.copy(quantity = item.quantity)
        } else {
            cart.remove(item)
        }
    }

    fun completeCheckout() {
        if (cart.isEmpty()) {
            Toast.makeText(context, "السلة فارغة", Toast.LENGTH_SHORT).show()
            return
        }
        // Deduct stock
        cart.forEach { item ->
            val pIdx = products.indexOfFirst { it.id == item.product.id }
            if (pIdx != -1) {
                val p = products[pIdx]
                products[pIdx] = p.copy(stock = (p.stock - item.quantity).coerceAtLeast(0))
            }
        }
        completedSaleReceipt = cart.toList()
        totalReceiptAmount = cartTotal
        showReceiptDialog = true

        // Clear cart
        cart.clear()
        customDiscountInput = ""
        Toast.makeText(context, "تمت عملية البيع وحفظ الفاتورة بنجاح!", Toast.LENGTH_LONG).show()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = VelvetRose,
            secondary = BlushPink,
            background = DeepCharcoal,
            surface = DeepCrimsonSurface
        )
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = DeepCrimsonSurface,
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = activeTab == "POS",
                        onClick = { activeTab = "POS" },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "POS") },
                        label = { Text("المبيعات POS", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentGold,
                            selectedTextColor = AccentGold,
                            indicatorColor = VelvetRose
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "Inventory",
                        onClick = { activeTab = "Inventory" },
                        icon = { Icon(Icons.Default.List, contentDescription = "Inventory") },
                        label = { Text("المخزون والمنتجات", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentGold,
                            selectedTextColor = AccentGold,
                            indicatorColor = VelvetRose
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "Reports",
                        onClick = { activeTab = "Reports" },
                        icon = { Icon(Icons.Default.Refresh, contentDescription = "Reports") },
                        label = { Text("التقارير المالية", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentGold,
                            selectedTextColor = AccentGold,
                            indicatorColor = VelvetRose
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepCharcoal)
                    .padding(paddingValues)
            ) {
                when (activeTab) {
                    "POS" -> PosTab(
                        filteredProducts = filteredProducts,
                        cart = cart,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        cartSubtotal = cartSubtotal,
                        appliedDiscount = appliedDiscount,
                        cartTotal = cartTotal,
                        customDiscountInput = customDiscountInput,
                        onCategorySelect = { selectedCategory = it },
                        onSearchChange = { searchQuery = it },
                        onDiscountInput = { customDiscountInput = it },
                        onAdd = { addToCart(it) },
                        onDecrease = { decreaseQuantity(it) },
                        onClearCart = { cart.clear() },
                        onCheckout = { completeCheckout() }
                    )
                    "Inventory" -> InventoryTab(
                        products = products,
                        onAddProduct = { newProduct ->
                            products.add(newProduct)
                            Toast.makeText(context, "تم إضافة المنتج بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        onUpdateStock = { pid, newStock ->
                            val idx = products.indexOfFirst { it.id == pid }
                            if (idx != -1) {
                                products[idx] = products[idx].copy(stock = newStock)
                            }
                        }
                    )
                    "Reports" -> ReportsTab(
                        totalSalesToday = 3450.0,
                        transactionCount = 18,
                        popularCategory = "العطور الفاخرة"
                    )
                }

                // Receipt Dialog popup
                if (showReceiptDialog && completedSaleReceipt != null) {
                    AlertDialog(
                        onDismissRequest = { showReceiptDialog = false },
                        title = {
                            Text(
                                text = "فاتورة بيع مطبوعة 🧾",
                                style = TextStyle(textDirection = TextDirection.Rtl),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                        },
                        text = {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                item {
                                    Text("محل BeautyPOS الفاخر للتجميل والعطور", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("تاريخ العملية: 2026-06-01", color = Color.White, fontSize = 13.sp)
                                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                                }
                                items(completedSaleReceipt!!) { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${item.product.price * item.quantity} ر.س", color = BlushPink)
                                        Text("${item.product.nameAr} x${item.quantity}", color = Color.White, textAlign = TextAlign.End)
                                    }
                                }
                                item {
                                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$totalReceiptAmount ر.س", color = AccentGold, fontWeight = FontWeight.Bold)
                                        Text("الإجمالي النهائي:", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("شكرًا لتسوقكم معنا! ✨", color = AccentGold, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { showReceiptDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = VelvetRose)
                            ) {
                                Text("إغلاق الفاتورة", color = Color.White)
                            }
                        },
                        containerColor = DeepCrimsonSurface
                    )
                }
            }
        }
    }
}

@Composable
fun PosTab(
    filteredProducts: List<Product>,
    cart: List<CartItem>,
    selectedCategory: String,
    searchQuery: String,
    cartSubtotal: Double,
    appliedDiscount: Double,
    cartTotal: Double,
    customDiscountInput: String,
    onCategorySelect: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onDiscountInput: (String) -> Unit,
    onAdd: (Product) -> Unit,
    onDecrease: (CartItem) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column: Cart & Summary Layout (takes 40% space)
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.4f)
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = DeepCrimsonSurface),
            border = BorderStroke(1.dp, VelvetRose)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "سلة المشتريات الحالية 🛒",
                    color = AccentGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Cart items list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (cart.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("السلة فارغة. اضغط على المنتجات لإضافتها.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        items(cart) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBackground)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Controls & subtotal
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onDecrease(item) }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Decrease", tint = Color.Red)
                                        }
                                        Text("${item.quantity}", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                        IconButton(onClick = { onAdd(item.product) }) {
                                            Icon(Icons.Default.Add, contentDescription = "Add", tint = BlushPink)
                                        }
                                    }

                                    // Product Name
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(item.product.nameAr, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                                        Text("${item.product.price} ر.س", color = BlushPink, fontSize = 11.sp, textAlign = TextAlign.End)
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

                // Discount addition & Receipt calculations
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customDiscountInput,
                            onValueChange = { onDiscountInput(it) },
                            placeholder = { Text("0.00", color = Color.Gray, fontSize = 12.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGold,
                                unfocusedBorderColor = VelvetRose
                            ),
                            modifier = Modifier.width(80.dp),
                            singleLine = true
                        )
                        Text("خصم يدوي (ر.س):", color = Color.White, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$cartSubtotal ر.س", color = Color.White)
                        Text("المجموع الفرعي:", color = Color.LightGray, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("-$appliedDiscount ر.س", color = Color.Red)
                        Text("الخصم المطبق:", color = Color.LightGray, fontSize = 13.sp)
                    }
                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$cartTotal ر.س", color = AccentGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("الإجمالي المستحق:", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onClearCart,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text("تفريغ", color = Color.White)
                    }
                    Button(
                        onClick = onCheckout,
                        colors = ButtonDefaults.buttonColors(containerColor = VelvetRose),
                        modifier = Modifier
                            .weight(2f)
                            .padding(start = 4.dp)
                    ) {
                        Text("إتمام الدفع واحتساب الفاتورة", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Right Column: Products inventory list for clicking (takes 60% space)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.6f)
                .padding(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                    placeholder = { Text("ابحث عن منتج بالاسم...", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = VelvetRose
                    ),
                    modifier = Modifier.weight(1f)
                )

                // App Branding Text
                Text(
                    text = "BeautyPOS ✨",
                    color = AccentGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Categories Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                listOf("All", "Makeup", "Perfumes", "Skincare").forEach { category ->
                    val display = when (category) {
                        "All" -> "الكل"
                        "Makeup" -> "مكياج 💄"
                        "Perfumes" -> "عطور 🧴"
                        "Skincare" -> "عناية بالبشرة"
                        else -> category
                    }
                    val isSelected = selectedCategory == category
                    Button(
                        onClick = { onCategorySelect(category) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) VelvetRose else CardBackground
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(display, color = if (isSelected) Color.White else BlushPink)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Products Grid View
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredProducts) { prod ->
                    val outOfStock = prod.stock <= 0
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAdd(prod) }
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = if (outOfStock) Color(51, 31, 35) else CardBackground),
                        border = BorderStroke(1.dp, if (outOfStock) Color.Red else Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = if (outOfStock) "غير متوفر ❌" else "متاح (${prod.stock})",
                                    color = if (outOfStock) Color.Red else Color.Green,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = prod.icon,
                                    fontSize = 24.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(prod.nameAr, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                            Text(prod.nameEn, color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("${prod.price} ر.س", color = AccentGold, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryTab(
    products: List<Product>,
    onAddProduct: (Product) -> Unit,
    onUpdateStock: (String, Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = VelvetRose)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة منتج جديد", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "جرد وإدارة المخزون والمنتجات 📦",
                color = AccentGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = DeepCrimsonSurface)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VelvetRose)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("العمليات", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("المخزون", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("السعر الحالي", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("المنتج بالتفصيل", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                items(products) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stock actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { onUpdateStock(item.id, item.stock + 5) },
                                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("+5 شحن", color = BlushPink, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp))
                            }
                        }

                        // Stock Value
                        Text("${item.stock} قطع", color = if (item.stock < 5) Color.Red else Color.Green, fontSize = 14.sp)

                        // Price
                        Text("${item.price} ر.س", color = AccentGold, fontSize = 14.sp)

                        // Name and Icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(item.nameAr, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(item.nameEn, color = Color.Gray, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(item.icon, fontSize = 20.sp)
                        }
                    }
                    Divider(color = Color.DarkGray)
                }
            }
        }

        if (showAddDialog) {
            var inputAr by remember { mutableStateOf("") }
            var inputEn by remember { mutableStateOf("") }
            var priceStr by remember { mutableStateOf("") }
            var stockStr by remember { mutableStateOf("") }
            var categorySelect by remember { mutableStateOf("Makeup") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("إدراج منتج جديد بالتصنيف", color = AccentGold, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        OutlinedTextField(
                            value = inputAr,
                            onValueChange = { inputAr = it },
                            placeholder = { Text("الاسم باللغة العربية") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = inputEn,
                            onValueChange = { inputEn = it },
                            placeholder = { Text("الاسم باللغة الإنجليزية") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            placeholder = { Text("سعر المستهلك (ر.س)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = stockStr,
                            onValueChange = { stockStr = it },
                            placeholder = { Text("الكمية البدئية") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val price = priceStr.toDoubleOrNull() ?: 0.0
                            val stock = stockStr.toIntOrNull() ?: 10
                            onAddProduct(
                                Product(
                                    id = System.currentTimeMillis().toString(),
                                    nameAr = inputAr,
                                    nameEn = inputEn,
                                    price = price,
                                    category = categorySelect,
                                    stock = stock,
                                    icon = "💄"
                                )
                            )
                            showAddDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VelvetRose)
                    ) {
                        Text("إضافة")
                    }
                },
                containerColor = DeepCrimsonSurface
            )
        }
    }
}

@Composable
fun ReportsTab(
    totalSalesToday: Double,
    transactionCount: Int,
    popularCategory: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "التقارير اليومية والأرباح 📊",
            color = AccentGold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("إجمالي المبيعات", color = BlushPink, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalSalesToday ر.س", color = AccentGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("عدد فواتير البيع", color = BlushPink, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$transactionCount عملية", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepCrimsonSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("أعلى تصنيف مبيعاً اليوم:", color = Color.Gray, fontSize = 13.sp)
                Text(popularCategory, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text("حالة السيرفر والاتصال: متصل وقناة قواعد البيانات نشطة ومؤمنة ☑️", color = Color.Green, fontSize = 12.sp)
            }
        }
    }
}
