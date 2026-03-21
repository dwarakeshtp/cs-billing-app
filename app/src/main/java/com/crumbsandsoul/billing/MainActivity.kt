package com.crumbsandsoul.billing

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Product(val name: String, val defaultPrice: Double)
data class Customer(val name: String, val phone: String, val gst: String = "")
data class InvoiceLineItem(var productName: String, var quantity: Double, var unitPrice: Double) {
    fun lineTotal(): Double = quantity * unitPrice
}

class InvoiceDraftState(initialInvoiceNumber: String) {
    var selectedCustomerName by mutableStateOf("")
    var selectedCustomerPhone by mutableStateOf("")
    var customerSearch by mutableStateOf("")
    var productSearch by mutableStateOf("")
    var quantityInput by mutableStateOf("1")
    var priceInput by mutableStateOf("")
    var invoiceNumber by mutableStateOf(initialInvoiceNumber)
    var invoiceDate by mutableStateOf("")
    var editingInvoiceNumber by mutableStateOf<String?>(null)
    var shippingChargesInput by mutableStateOf("")
    val lineItems = mutableStateListOf<InvoiceLineItem>()
}

data class InvoiceRecord(
    val invoiceNumber: String,
    val invoiceDate: String,
    val customerName: String,
    val customerPhone: String,
    val items: List<InvoiceLineItem>,
    val shippingCharges: Double,
    val total: Double,
    val filePath: String,
    val createdAtMillis: Long,
    val paymentReceived: Boolean
)

enum class AppSection(val title: String) {
    GenerateInvoice("Generate Invoice"),
    AddProduct("Add Product"),
    AddCustomer("Add Customer"),
    InvoiceHistory("Invoice History"),
    SalesReports("Sales Reports")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrumbsAndSoulTheme {
                BillingApp()
            }
        }
    }
}

private val BrandLightColors = lightColorScheme(
    primary = ComposeColor(0xFF5E6840),
    onPrimary = ComposeColor(0xFFFFFFFF),
    secondary = ComposeColor(0xFFBC9B56),
    onSecondary = ComposeColor(0xFF1B1B1B),
    tertiary = ComposeColor(0xFF7A5A1E),
    background = ComposeColor(0xFFF7F4EA),
    onBackground = ComposeColor(0xFF161616),
    surface = ComposeColor(0xFFFFFCF4),
    onSurface = ComposeColor(0xFF1B1B1B),
    surfaceVariant = ComposeColor(0xFFEEE8DA),
    onSurfaceVariant = ComposeColor(0xFF4A4A4A),
    outline = ComposeColor(0xFFB4AE9E)
)

private val BrandDarkColors = darkColorScheme(
    primary = ComposeColor(0xFF9AA77A),
    onPrimary = ComposeColor(0xFF1E2910),
    secondary = ComposeColor(0xFFD2B475),
    onSecondary = ComposeColor(0xFF2A2113),
    background = ComposeColor(0xFF12120F),
    onBackground = ComposeColor(0xFFF4F0E5),
    surface = ComposeColor(0xFF1B1A16),
    onSurface = ComposeColor(0xFFF4F0E5),
    surfaceVariant = ComposeColor(0xFF2A2923),
    onSurfaceVariant = ComposeColor(0xFFD0CABD),
    outline = ComposeColor(0xFF8A8477)
)

@Composable
fun CrumbsAndSoulTheme(content: @Composable () -> Unit) {
    val useDark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (useDark) BrandDarkColors else BrandLightColors,
        content = content
    )
}

@Composable
fun BillingApp() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val storage = remember { BillingStorage(context) }
    var section by remember { mutableStateOf(AppSection.GenerateInvoice) }
    val products = remember { mutableStateListOf<Product>().apply { addAll(storage.loadProducts()) } }
    val customers = remember { mutableStateListOf<Customer>().apply { addAll(storage.loadCustomers()) } }
    val invoiceHistory = remember { mutableStateListOf<InvoiceRecord>().apply { addAll(storage.loadInvoiceHistory()) } }
    val invoiceDraft = remember { InvoiceDraftState(storage.previewInvoiceNumber()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComposeColor.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AppSection.entries.forEach { destination ->
                    val selected = destination == section
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { section = destination }
                            .background(
                                if (selected) ComposeColor(0xFFECE6FF) else ComposeColor.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(
                            modifier = Modifier
                                .width(26.dp)
                                .height(3.dp)
                                .background(
                                    if (selected) ComposeColor(0xFF5E6840) else ComposeColor.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = destination.title,
                            textAlign = TextAlign.Center,
                            color = if (selected) ComposeColor(0xFF5E6840) else ComposeColor(0xFF2B2B2B),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Final)
                            if (event.type == PointerEventType.Release && event.changes.all { !it.isConsumed }) {
                                focusManager.clearFocus()
                            }
                        }
                    }
                }
                .background(ComposeColor(0xFFF7F4EA))
                .padding(16.dp)
        ) {
            when (section) {
                AppSection.GenerateInvoice -> InvoiceScreen(
                    products = products,
                    customers = customers,
                    draft = invoiceDraft,
                    onSavePdf = { invoice, isUpdate ->
                        val file = InvoicePdfGenerator.createInvoicePdf(context, invoice)
                        if (file != null) {
                            val existingIndex = invoiceHistory.indexOfFirst { it.invoiceNumber == invoice.invoiceNumber }
                            val existing = if (existingIndex >= 0) invoiceHistory[existingIndex] else null
                            val record = InvoiceRecord(
                                invoiceNumber = invoice.invoiceNumber,
                                invoiceDate = invoice.invoiceDate,
                                customerName = invoice.customerName,
                                customerPhone = invoice.customerPhone,
                                items = invoice.items,
                                shippingCharges = invoice.shippingCharges,
                                total = invoice.total,
                                filePath = file.absolutePath,
                                createdAtMillis = existing?.createdAtMillis ?: System.currentTimeMillis(),
                                paymentReceived = existing?.paymentReceived ?: false
                            )
                            if (isUpdate && existingIndex >= 0) {
                                invoiceHistory[existingIndex] = record
                            } else {
                                invoiceHistory.add(0, record)
                            }
                            storage.saveInvoiceHistory(invoiceHistory.toList())
                            scope.launch { snackbarHostState.showSnackbar(if (isUpdate) "Updated: ${file.name}" else "Saved: ${file.name}") }
                            file
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Failed to save invoice PDF") }
                            null
                        }
                    },
                    onShareWhatsapp = { invoice, isUpdate ->
                        val file = InvoicePdfGenerator.createInvoicePdf(context, invoice)
                        if (file != null) {
                            val existingIndex = invoiceHistory.indexOfFirst { it.invoiceNumber == invoice.invoiceNumber }
                            val existing = if (existingIndex >= 0) invoiceHistory[existingIndex] else null
                            val record = InvoiceRecord(
                                invoiceNumber = invoice.invoiceNumber,
                                invoiceDate = invoice.invoiceDate,
                                customerName = invoice.customerName,
                                customerPhone = invoice.customerPhone,
                                items = invoice.items,
                                shippingCharges = invoice.shippingCharges,
                                total = invoice.total,
                                filePath = file.absolutePath,
                                createdAtMillis = existing?.createdAtMillis ?: System.currentTimeMillis(),
                                paymentReceived = existing?.paymentReceived ?: false
                            )
                            if (isUpdate && existingIndex >= 0) {
                                invoiceHistory[existingIndex] = record
                            } else {
                                invoiceHistory.add(0, record)
                            }
                            storage.saveInvoiceHistory(invoiceHistory.toList())
                            shareOnWhatsApp(context, file, invoice.customerPhone)
                            file
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Failed to generate invoice PDF") }
                            null
                        }
                    },
                    previewInvoiceNumber = { storage.previewInvoiceNumber() },
                    consumeInvoiceNumber = { storage.consumeInvoiceNumber() },
                    resetDraft = {
                        invoiceDraft.selectedCustomerName = ""
                        invoiceDraft.selectedCustomerPhone = ""
                        invoiceDraft.customerSearch = ""
                        invoiceDraft.productSearch = ""
                        invoiceDraft.quantityInput = "1"
                        invoiceDraft.priceInput = ""
                        invoiceDraft.invoiceDate = ""
                        invoiceDraft.editingInvoiceNumber = null
                        invoiceDraft.shippingChargesInput = ""
                        invoiceDraft.lineItems.clear()
                        invoiceDraft.invoiceNumber = storage.previewInvoiceNumber()
                    }
                )
                AppSection.AddProduct -> ProductScreen(
                    products = products,
                    context = context,
                    onExport = {
                        exportJsonAndShare(
                            context = context,
                            fileName = "products_backup.json",
                            json = storage.exportProductsJson()
                        )
                    },
                    onAdd = { name, price ->
                        products.add(Product(name.trim(), price))
                        storage.saveProducts(products.toList())
                    },
                    onDelete = { index ->
                        products.removeAt(index)
                        storage.saveProducts(products.toList())
                    }
                )
                AppSection.AddCustomer -> CustomerScreen(
                    customers = customers,
                    context = context,
                    onExport = {
                        exportJsonAndShare(
                            context = context,
                            fileName = "customers_backup.json",
                            json = storage.exportCustomersJson()
                        )
                    },
                    onAdd = { name, phone, gst ->
                        customers.add(Customer(name.trim(), phone.trim(), gst.trim()))
                        storage.saveCustomers(customers.toList())
                    },
                    onDelete = { index ->
                        customers.removeAt(index)
                        storage.saveCustomers(customers.toList())
                    }
                )
                AppSection.InvoiceHistory -> InvoiceHistoryScreen(
                    records = invoiceHistory,
                    products = products,
                    onUpdateStatus = { invoiceNumber, received ->
                        val index = invoiceHistory.indexOfFirst { it.invoiceNumber == invoiceNumber }
                        if (index >= 0) {
                            val old = invoiceHistory[index]
                            invoiceHistory[index] = old.copy(paymentReceived = received)
                            storage.saveInvoiceHistory(invoiceHistory.toList())
                        }
                    },
                    onEditInvoice = { updated ->
                        val index = invoiceHistory.indexOfFirst { it.invoiceNumber == updated.invoiceNumber }
                        if (index >= 0) {
                            invoiceHistory[index] = updated
                            storage.saveInvoiceHistory(invoiceHistory.toList())
                        }
                    },
                    onDeleteInvoice = { record ->
                        invoiceHistory.removeAll { it.invoiceNumber == record.invoiceNumber }
                        storage.saveInvoiceHistory(invoiceHistory.toList())
                        val file = File(record.filePath)
                        if (file.exists()) file.delete()
                    }
                )
                AppSection.SalesReports -> SalesReportScreen(records = invoiceHistory)
            }
        }
    }
}

data class InvoiceData(
    val invoiceNumber: String,
    val invoiceDate: String,
    val customerName: String,
    val customerPhone: String,
    val items: List<InvoiceLineItem>,
    val shippingCharges: Double,
    val total: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    products: List<Product>,
    customers: List<Customer>,
    draft: InvoiceDraftState,
    onSavePdf: (InvoiceData, Boolean) -> File?,
    onShareWhatsapp: (InvoiceData, Boolean) -> File?,
    previewInvoiceNumber: () -> String,
    consumeInvoiceNumber: () -> String,
    resetDraft: () -> Unit
) {
    val context = LocalContext.current
    var selectedCustomer by remember {
        mutableStateOf(
            if (draft.selectedCustomerName.isNotBlank()) {
                Customer(draft.selectedCustomerName, draft.selectedCustomerPhone)
            } else null
        )
    }
    var customerSearch by remember { mutableStateOf(draft.customerSearch) }
    var customerSuggestionsVisible by remember { mutableStateOf(false) }
    val lineItems = draft.lineItems
    var productSearch by remember { mutableStateOf(draft.productSearch) }
    var productSuggestionsVisible by remember { mutableStateOf(false) }
    var quantityInput by remember { mutableStateOf(draft.quantityInput) }
    var priceInput by remember { mutableStateOf(draft.priceInput) }
    var shippingChargesInput by remember { mutableStateOf(draft.shippingChargesInput) }
    var invoiceNumber by remember { mutableStateOf(draft.invoiceNumber.ifBlank { previewInvoiceNumber() }) }
    var editIndex by remember { mutableStateOf(-1) }
    var editName by remember { mutableStateOf("") }
    var editQty by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }
    var lastGeneratedFile by remember { mutableStateOf<File?>(null) }
    val today = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    var invoiceDate by remember { mutableStateOf(draft.invoiceDate.ifBlank { today }) }

    val filteredCustomers = customers.filter {
        it.name.contains(customerSearch, ignoreCase = true) || it.phone.contains(customerSearch)
    }
    val filteredProducts = products.filter { it.name.contains(productSearch, ignoreCase = true) }
    val subTotal = lineItems.sumOf { it.lineTotal() }
    val shippingCharges = shippingChargesInput.toDoubleOrNull() ?: 0.0
    val total = subTotal + shippingCharges

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
    ) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Generate Invoice", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Crumbs & Soul", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Invoice No: $invoiceNumber", fontWeight = FontWeight.SemiBold)
            Text("Date: $invoiceDate")
            if (draft.editingInvoiceNumber != null) {
                Text("Editing Existing Invoice", color = ComposeColor(0xFF5E6840), fontWeight = FontWeight.SemiBold)
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = if (selectedCustomer != null) selectedCustomer!!.name else customerSearch,
        onValueChange = {
            selectedCustomer = null
            customerSearch = it
            customerSuggestionsVisible = true
        },
        label = { Text("Search customer") },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        )
    )
    if (customerSuggestionsVisible && selectedCustomer == null && customerSearch.isNotBlank() && filteredCustomers.isNotEmpty()) {
        Card(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                filteredCustomers.take(5).forEach { customer ->
                    TextButton(
                        onClick = {
                            selectedCustomer = customer
                            customerSearch = customer.name
                            customerSuggestionsVisible = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${customer.name} - ${customer.phone}", modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text("Add Line Item", fontWeight = FontWeight.SemiBold)
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = productSearch,
        onValueChange = {
            productSearch = it
            productSuggestionsVisible = true
        },
        label = { Text("Product name (type to search)") },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        )
    )
    if (productSuggestionsVisible && productSearch.isNotBlank() && filteredProducts.isNotEmpty()) {
        Card(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                filteredProducts.take(6).forEach { product ->
                    TextButton(
                        onClick = {
                            productSearch = product.name
                            priceInput = product.defaultPrice.toString()
                            productSuggestionsVisible = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(product.name, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = quantityInput,
            onValueChange = { quantityInput = it },
            label = { Text("Quantity") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = priceInput,
            onValueChange = { priceInput = it },
            label = { Text("Unit price") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )
        Text(
            "Default product price is prefilled. You can override it before adding.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }

    Button(
        modifier = Modifier.padding(top = 8.dp),
        onClick = {
            val quantity = quantityInput.toDoubleOrNull()
            val price = priceInput.toDoubleOrNull()
            if (productSearch.isNotBlank() && quantity != null && price != null) {
                lineItems.add(InvoiceLineItem(productSearch.trim(), quantity, price))
                productSearch = ""
                productSuggestionsVisible = false
                quantityInput = "1"
                priceInput = ""
            }
        }
    ) { Text("Add Item") }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .verticalScroll(rememberScrollState())
    ) {
        lineItems.forEachIndexed { index, item ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(item.productName, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Qty: ${item.quantity} x ₹${money(item.unitPrice)} = ₹${money(item.lineTotal())}")
                        Row {
                            IconButton(onClick = {
                                editIndex = index
                                editName = item.productName
                                editQty = item.quantity.toString()
                                editPrice = item.unitPrice.toString()
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit line item")
                            }
                            IconButton(onClick = { lineItems.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete line item")
                            }
                        }
                    }
                }
            }
        }
    }
    HorizontalDivider()
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        value = shippingChargesInput,
        onValueChange = { shippingChargesInput = it },
        label = { Text("Shipping Charges (Optional)") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        )
    )
    Text(
        text = "Subtotal: ₹${money(subTotal)}",
        fontSize = 14.sp,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.End,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    if (shippingCharges > 0.0) {
        Text(
            text = "Shipping: ₹${money(shippingCharges)}",
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Text(
        text = "Total: ₹${money(total)}",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.End
    )
    Spacer(modifier = Modifier.height(16.dp))
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (lineItems.isNotEmpty()) {
                    val invoice = InvoiceData(
                        invoiceNumber = invoiceNumber,
                        invoiceDate = invoiceDate,
                        customerName = selectedCustomer?.name ?: customerSearch.ifBlank { "Walk-in Customer" },
                        customerPhone = selectedCustomer?.phone ?: "",
                        items = lineItems.toList(),
                        shippingCharges = shippingCharges,
                        total = total
                    )
                    val isUpdate = draft.editingInvoiceNumber != null
                    lastGeneratedFile = onSavePdf(invoice, isUpdate)
                    if (!isUpdate) {
                        consumeInvoiceNumber()
                    }
                    invoiceNumber = previewInvoiceNumber()
                    invoiceDate = today
                    resetDraft()
                    selectedCustomer = null
                    customerSearch = ""
                    productSearch = ""
                    quantityInput = "1"
                    priceInput = ""
                    shippingChargesInput = ""
                }
            }
        ) { Text("Generate Invoice PDF") }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (lineItems.isNotEmpty()) {
                    val invoice = InvoiceData(
                        invoiceNumber = invoiceNumber,
                        invoiceDate = invoiceDate,
                        customerName = selectedCustomer?.name ?: customerSearch.ifBlank { "Walk-in Customer" },
                        customerPhone = selectedCustomer?.phone ?: "",
                        items = lineItems.toList(),
                        shippingCharges = shippingCharges,
                        total = total
                    )
                    if (invoice.customerPhone.isBlank()) {
                        Toast.makeText(context, "Select customer with phone number to share on WhatsApp", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val isUpdate = draft.editingInvoiceNumber != null
                    lastGeneratedFile = onShareWhatsapp(invoice, isUpdate)
                    if (!isUpdate) {
                        consumeInvoiceNumber()
                    }
                    invoiceNumber = previewInvoiceNumber()
                    invoiceDate = today
                    resetDraft()
                    selectedCustomer = null
                    customerSearch = ""
                    productSearch = ""
                    quantityInput = "1"
                    priceInput = ""
                    shippingChargesInput = ""
                }
            }
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.size(4.dp))
            Text("Generate & Share WhatsApp")
        }
    }
    }
    Spacer(modifier = Modifier.height(8.dp))

    draft.selectedCustomerName = selectedCustomer?.name ?: ""
    draft.selectedCustomerPhone = selectedCustomer?.phone ?: ""
    draft.customerSearch = customerSearch
    draft.productSearch = productSearch
    draft.quantityInput = quantityInput
    draft.priceInput = priceInput
    draft.shippingChargesInput = shippingChargesInput
    draft.invoiceNumber = invoiceNumber
    draft.invoiceDate = invoiceDate

    if (editIndex >= 0) {
        AlertDialog(
            onDismissRequest = { editIndex = -1 },
            title = { Text("Edit line item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Product") }
                    )
                    OutlinedTextField(
                        value = editQty,
                        onValueChange = { editQty = it },
                        label = { Text("Quantity") }
                    )
                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it },
                        label = { Text("Unit price") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val qty = editQty.toDoubleOrNull()
                    val unit = editPrice.toDoubleOrNull()
                    if (qty != null && unit != null && editName.isNotBlank() && editIndex in lineItems.indices) {
                        lineItems[editIndex] = InvoiceLineItem(editName.trim(), qty, unit)
                        editIndex = -1
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editIndex = -1 }) { Text("Cancel") }
            }
        )
    }
}
}


@Composable
fun ProductScreen(
    products: List<Product>,
    context: Context,
    onExport: () -> Unit,
    onAdd: (String, Double) -> Unit,
    onDelete: (Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duplicateNameError by remember { mutableStateOf(false) }

    Text("Add Product", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    duplicateNameError = false
                },
                isError = duplicateNameError,
                label = { Text("Product name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
    if (duplicateNameError) {
        Text(
            "Product with this name already exists",
            color = ComposeColor(0xFFB00020),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
        )
    }
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Default unit price") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                )
            )
            Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val p = price.toDoubleOrNull()
            if (name.isNotBlank() && p != null) {
                val normalized = name.trim().lowercase(Locale.getDefault())
                val duplicateExists = products.any { it.name.trim().lowercase(Locale.getDefault()) == normalized }
                if (duplicateExists) {
                    duplicateNameError = true
                    Toast.makeText(context, "Duplicate product name not allowed", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                onAdd(name, p)
                name = ""
                price = ""
                duplicateNameError = false
            }
        }
    ) { Text("Save Product") }
        }
    }
    TextButton(onClick = onExport) { Text("Export Products Backup") }

    Spacer(modifier = Modifier.height(12.dp))
    LazyColumn {
        itemsIndexed(products) { index, product ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${product.name} - ₹${money(product.defaultPrice)}")
                TextButton(onClick = { onDelete(index) }) { Text("Delete") }
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun CustomerScreen(
    customers: List<Customer>,
    context: Context,
    onExport: () -> Unit,
    onAdd: (String, String, String) -> Unit,
    onDelete: (Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gst by remember { mutableStateOf("") }
    var showValidation by remember { mutableStateOf(false) }
    var duplicatePhoneError by remember { mutableStateOf(false) }
    val nameError = showValidation && name.isBlank()
    val phoneError = showValidation && (phone.isBlank() || normalizeWhatsAppPhone(phone) == null)

    Text("Add Customer", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
        value = name,
        onValueChange = {
            name = it
            if (showValidation) showValidation = true
        },
        isError = nameError,
        label = { Text("Name *") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        )
    )
    if (nameError) {
        Text(
            "Customer name is required",
            color = ComposeColor(0xFFB00020),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
        )
    }
            OutlinedTextField(
        value = phone,
        onValueChange = {
            phone = it.filter { ch -> ch.isDigit() }.take(10)
            duplicatePhoneError = false
            if (showValidation) showValidation = true
        },
        isError = phoneError,
        label = { Text("Phone number *") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        )
    )
    if (phoneError) {
        Text(
            "Enter valid phone (10-digit or country code format)",
            color = ComposeColor(0xFFB00020),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
        )
    } else if (duplicatePhoneError) {
        Text(
            "Customer with this phone number already exists",
            color = ComposeColor(0xFFB00020),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
        )
    }
            OutlinedTextField(value = gst, onValueChange = { gst = it }, label = { Text("GST (Optional)") }, modifier = Modifier.fillMaxWidth())
            Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            showValidation = true
            if (name.isNotBlank() && phone.isNotBlank()) {
                val normalized = normalizeWhatsAppPhone(phone)
                if (normalized == null) {
                    Toast.makeText(context, "Enter valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val duplicateExists = customers.any {
                    normalizeWhatsAppPhone(it.phone) == normalized
                }
                if (duplicateExists) {
                    duplicatePhoneError = true
                    Toast.makeText(context, "Duplicate phone number not allowed", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                onAdd(name, formatPhoneForDisplay(normalized), gst)
                name = ""
                phone = ""
                gst = ""
                showValidation = false
                duplicatePhoneError = false
            } else {
                Toast.makeText(context, "Name and phone number are mandatory", Toast.LENGTH_SHORT).show()
            }
        }
    ) { Text("Save Customer") }
        }
    }
    TextButton(onClick = onExport) { Text("Export Customers Backup") }

    Spacer(modifier = Modifier.height(12.dp))
    LazyColumn {
        itemsIndexed(customers) { index, customer ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${customer.name} - ${customer.phone}")
                TextButton(onClick = { onDelete(index) }) { Text("Delete") }
            }
            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHistoryScreen(
    records: List<InvoiceRecord>,
    products: List<Product>,
    onUpdateStatus: (String, Boolean) -> Unit,
    onEditInvoice: (InvoiceRecord) -> Unit,
    onDeleteInvoice: (InvoiceRecord) -> Unit
) {
    val context = LocalContext.current
    val customerOptions = buildList {
        add("All Customers")
        addAll(records.map { it.customerName.ifBlank { "Walk-in Customer" } }.distinct().sorted())
    }
    val dateRangeOptions = listOf("All Time", "Last 7 Days", "Last 30 Days", "Last 90 Days")
    val paymentOptions = listOf("All", "Received", "Pending")
    var customerFilter by remember { mutableStateOf(customerOptions.first()) }
    var dateFilter by remember { mutableStateOf(dateRangeOptions.first()) }
    var paymentFilter by remember { mutableStateOf(paymentOptions.first()) }
    var customerExpanded by remember { mutableStateOf(false) }
    var dateExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<InvoiceRecord?>(null) }
    var editingRecord by remember { mutableStateOf<InvoiceRecord?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editShipping by remember { mutableStateOf("") }
    val editItems = remember { mutableStateListOf<InvoiceLineItem>() }
    var activeProductIndex by remember { mutableStateOf(-1) }

    val filtered = records.filter { record ->
        val customerMatch = customerFilter == "All Customers" || record.customerName.ifBlank { "Walk-in Customer" } == customerFilter
        val paymentMatch = when (paymentFilter) {
            "Received" -> record.paymentReceived
            "Pending" -> !record.paymentReceived
            else -> true
        }
        val dateMatch = when (dateFilter) {
            "Last 7 Days" -> isWithinDays(record.createdAtMillis, 7)
            "Last 30 Days" -> isWithinDays(record.createdAtMillis, 30)
            "Last 90 Days" -> isWithinDays(record.createdAtMillis, 90)
            else -> true
        }
        customerMatch && paymentMatch && dateMatch
    }

    Text("Invoice History", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    DropdownField(
        label = "Customer Filter",
        value = customerFilter,
        options = customerOptions,
        expanded = customerExpanded,
        onExpanded = { customerExpanded = it },
        onSelect = { customerFilter = it }
    )
    DropdownField(
        label = "Date Filter",
        value = dateFilter,
        options = dateRangeOptions,
        expanded = dateExpanded,
        onExpanded = { dateExpanded = it },
        onSelect = { dateFilter = it }
    )
    DropdownField(
        label = "Payment Filter",
        value = paymentFilter,
        options = paymentOptions,
        expanded = paymentExpanded,
        onExpanded = { paymentExpanded = it },
        onSelect = { paymentFilter = it }
    )

    if (filtered.isEmpty()) {
        Text("No invoices yet.")
        return
    }
    LazyColumn {
        itemsIndexed(filtered) { _, record ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    val tileColor = if (record.paymentReceived) ComposeColor(0xFFE9F8EC) else ComposeColor(0xFFFFEBEB)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(tileColor, RoundedCornerShape(10.dp))
                            .clickable { onUpdateStatus(record.invoiceNumber, !record.paymentReceived) }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (record.paymentReceived) "Payment Received" else "Payment Pending",
                            color = if (record.paymentReceived) ComposeColor(0xFF1E7E34) else ComposeColor(0xFFB00020),
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { onUpdateStatus(record.invoiceNumber, !record.paymentReceived) }) {
                            Icon(
                                if (record.paymentReceived) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                contentDescription = "Toggle payment status"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Invoice: ${record.invoiceNumber}", fontWeight = FontWeight.Bold)
                    Text("Date: ${record.invoiceDate}")
                    Text("Customer: ${record.customerName}")
                    Text("Total: ₹${money(record.total)}")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        IconButton(
                            onClick = {
                                editingRecord = record
                                editName = record.customerName
                                editPhone = record.customerPhone
                                editShipping = if (record.shippingCharges > 0.0) money(record.shippingCharges) else ""
                                editItems.clear()
                                editItems.addAll(record.items.map { InvoiceLineItem(it.productName, it.quantity, it.unitPrice) })
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit invoice")
                        }
                        IconButton(onClick = {
                            val file = File(record.filePath)
                            if (file.exists()) {
                                openPdfFile(context, file)
                            } else {
                                Toast.makeText(context, "PDF not found on device", Toast.LENGTH_SHORT).show()
                            }
                        }, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.Default.Visibility, contentDescription = "View invoice")
                        }
                        IconButton(onClick = {
                            val file = File(record.filePath)
                            if (file.exists()) {
                                if (record.customerPhone.isNotBlank()) {
                                    shareOnWhatsApp(context, file, record.customerPhone)
                                } else {
                                    Toast.makeText(context, "Customer phone missing for WhatsApp share", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "PDF not found on device", Toast.LENGTH_SHORT).show()
                            }
                        }, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Share invoice via WhatsApp")
                        }
                        IconButton(onClick = { pendingDelete = record }, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete invoice")
                        }
                    }
                }
            }
        }
    }

    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete Invoice") },
            text = { Text("Are you sure you want to delete invoice ${pendingDelete!!.invoiceNumber}? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteInvoice(pendingDelete!!)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (editingRecord != null) {
        AlertDialog(
            onDismissRequest = { editingRecord = null },
            title = { Text("Edit Invoice") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Customer Name") }
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it.filter { ch -> ch.isDigit() }.take(10) },
                        label = { Text("Customer Phone") }
                    )
                    OutlinedTextField(
                        value = editShipping,
                        onValueChange = { editShipping = it },
                        label = { Text("Shipping Charges") }
                    )
                    Text(
                        "Leave shipping empty or 0 to remove it.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text("Line Items", fontWeight = FontWeight.SemiBold)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        editItems.forEachIndexed { index, item ->
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = item.productName,
                                        onValueChange = {
                                            editItems[index] = item.copy(productName = it)
                                            activeProductIndex = index
                                        },
                                        label = { Text("Product") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    val matches = if (activeProductIndex == index) {
                                        products.filter { it.name.contains(item.productName, ignoreCase = true) }.take(5)
                                    } else emptyList()
                                    if (item.productName.isNotBlank() && matches.isNotEmpty()) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Column {
                                                matches.forEach { p ->
                                                    TextButton(
                                                        onClick = {
                                                            editItems[index] = item.copy(productName = p.name, unitPrice = p.defaultPrice)
                                                            activeProductIndex = -1
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(p.name, modifier = Modifier.fillMaxWidth())
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = item.quantity.toString(),
                                            onValueChange = { value ->
                                                val qty = value.toDoubleOrNull()
                                                if (qty != null) {
                                                    editItems[index] = item.copy(quantity = qty)
                                                }
                                            },
                                            label = { Text("Qty") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = item.unitPrice.toString(),
                                            onValueChange = { value ->
                                                val price = value.toDoubleOrNull()
                                                if (price != null) {
                                                    editItems[index] = item.copy(unitPrice = price)
                                                }
                                            },
                                            label = { Text("Unit Price") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { editItems.removeAt(index) },
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove item")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    TextButton(onClick = {
                        editItems.add(InvoiceLineItem(productName = "", quantity = 1.0, unitPrice = 0.0))
                    }) {
                        Text("Add Item")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val record = editingRecord ?: return@TextButton
                    val shipping = editShipping.toDoubleOrNull() ?: 0.0
                    val sanitizedItems = editItems
                        .filter { it.productName.isNotBlank() && it.quantity > 0.0 && it.unitPrice >= 0.0 }
                        .map { InvoiceLineItem(it.productName.trim(), it.quantity, it.unitPrice) }
                    if (sanitizedItems.isEmpty()) {
                        Toast.makeText(context, "Add at least one valid line item", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    val normalizedPhone = if (editPhone.isBlank()) "" else (normalizeWhatsAppPhone(editPhone) ?: run {
                        Toast.makeText(context, "Enter valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    })
                    val displayPhone = if (normalizedPhone.isNotBlank()) formatPhoneForDisplay(normalizedPhone) else ""
                    val newTotal = sanitizedItems.sumOf { it.lineTotal() } + shipping
                    val updated = record.copy(
                        customerName = editName.ifBlank { record.customerName },
                        customerPhone = displayPhone,
                        items = sanitizedItems,
                        shippingCharges = shipping,
                        total = newTotal
                    )
                    // Regenerate PDF file so updated invoice data is reflected.
                    val newPdf = InvoicePdfGenerator.createInvoicePdf(
                        context,
                        InvoiceData(
                            invoiceNumber = updated.invoiceNumber,
                            invoiceDate = updated.invoiceDate,
                            customerName = updated.customerName,
                            customerPhone = updated.customerPhone,
                            items = updated.items,
                            shippingCharges = updated.shippingCharges,
                            total = updated.total
                        )
                    )
                    onEditInvoice(
                        if (newPdf != null) updated.copy(filePath = newPdf.absolutePath) else updated
                    )
                    editingRecord = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingRecord = null }) { Text("Cancel") }
            }
        )
    }
}

data class ReportRow(val key: String, val invoiceCount: Int, val totalSales: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportScreen(records: List<InvoiceRecord>) {
    val context = LocalContext.current
    val reportTypeOptions = listOf("Month-wise", "Customer-wise")
    val formatOptions = listOf("PDF", "Excel")
    val monthOptions = buildList {
        add("All Months")
        addAll(records.map { monthKey(it.createdAtMillis) }.distinct().sortedDescending())
    }
    val customerOptions = buildList {
        add("All Customers")
        addAll(records.map { it.customerName }.distinct().sorted())
    }

    var reportType by remember { mutableStateOf(reportTypeOptions.first()) }
    var exportFormat by remember { mutableStateOf(formatOptions.first()) }
    var monthFilter by remember { mutableStateOf(monthOptions.first()) }
    var customerFilter by remember { mutableStateOf(customerOptions.first()) }
    var reportTypeExpanded by remember { mutableStateOf(false) }
    var formatExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var customerExpanded by remember { mutableStateOf(false) }

    val filtered = records.filter {
        val monthMatch = monthFilter == "All Months" || monthKey(it.createdAtMillis) == monthFilter
        val customerMatch = customerFilter == "All Customers" || it.customerName == customerFilter
        monthMatch && customerMatch
    }
    val rows = buildReportRows(filtered, reportType)

    Text("Sales Reports", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    DropdownField(
        label = "Report Type",
        value = reportType,
        options = reportTypeOptions,
        expanded = reportTypeExpanded,
        onExpanded = { reportTypeExpanded = it },
        onSelect = { reportType = it }
    )
    DropdownField(
        label = "Month Filter",
        value = monthFilter,
        options = monthOptions,
        expanded = monthExpanded,
        onExpanded = { monthExpanded = it },
        onSelect = { monthFilter = it }
    )
    DropdownField(
        label = "Customer Filter",
        value = customerFilter,
        options = customerOptions,
        expanded = customerExpanded,
        onExpanded = { customerExpanded = it },
        onSelect = { customerFilter = it }
    )
    DropdownField(
        label = "Export Format",
        value = exportFormat,
        options = formatOptions,
        expanded = formatExpanded,
        onExpanded = { formatExpanded = it },
        onSelect = { exportFormat = it }
    )

    Spacer(modifier = Modifier.height(8.dp))
    Text("Preview", fontWeight = FontWeight.SemiBold)
    if (rows.isEmpty()) {
        Text("No data for selected filters.")
    } else {
        LazyColumn(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            itemsIndexed(rows.take(10)) { _, row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(row.key)
                    Text("${row.invoiceCount} invoices | ₹${money(row.totalSales)}")
                }
                HorizontalDivider()
            }
        }
    }

    Button(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        onClick = {
            if (rows.isEmpty()) {
                Toast.makeText(context, "No report data to export", Toast.LENGTH_SHORT).show()
                return@Button
            }
            val file = if (exportFormat == "PDF") {
                createSalesReportPdf(context, reportType, monthFilter, customerFilter, rows)
            } else {
                createSalesReportExcel(context, reportType, monthFilter, customerFilter, rows)
            }
            if (file != null) {
                Toast.makeText(context, "Saved: ${file.name}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to export report", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        Text("Generate & Export")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    expanded: Boolean,
    onExpanded: (Boolean) -> Unit,
    onSelect: (String) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { onExpanded(!expanded) }) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = value,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpanded(false) }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        onExpanded(false)
                    }
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

fun buildReportRows(records: List<InvoiceRecord>, reportType: String): List<ReportRow> {
    val grouped = if (reportType == "Month-wise") {
        records.groupBy { monthKey(it.createdAtMillis) }
    } else {
        records.groupBy { it.customerName.ifBlank { "Walk-in Customer" } }
    }
    return grouped.entries
        .map { (key, list) ->
            ReportRow(key = key, invoiceCount = list.size, totalSales = list.sumOf { it.total })
        }
        .sortedByDescending { it.totalSales }
}

fun monthKey(millis: Long): String {
    if (millis <= 0L) return "Unknown Month"
    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return date.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()))
}

fun isWithinDays(millis: Long, days: Int): Boolean {
    if (millis <= 0L) return false
    val now = System.currentTimeMillis()
    val diff = now - millis
    return diff <= days * 24L * 60L * 60L * 1000L
}

fun money(value: Double): String {
    return BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toPlainString()
}

class BillingStorage(context: Context) {
    private val prefs = context.getSharedPreferences("billing_store", Context.MODE_PRIVATE)
    private val productsKey = "products"
    private val customersKey = "customers"
    private val invoiceSequenceKey = "invoice_sequence"
    private val invoiceHistoryKey = "invoice_history"

    fun loadProducts(): List<Product> {
        val raw = prefs.getString(productsKey, "[]") ?: "[]"
        val arr = JSONArray(raw)
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(Product(obj.getString("name"), obj.getDouble("defaultPrice")))
            }
        }
    }

    fun saveProducts(items: List<Product>) {
        val arr = JSONArray()
        items.forEach {
            arr.put(
                JSONObject().apply {
                    put("name", it.name)
                    put("defaultPrice", it.defaultPrice)
                }
            )
        }
        prefs.edit().putString(productsKey, arr.toString()).apply()
    }

    fun loadCustomers(): List<Customer> {
        val raw = prefs.getString(customersKey, "[]") ?: "[]"
        val arr = JSONArray(raw)
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(Customer(obj.getString("name"), obj.getString("phone"), obj.optString("gst", "")))
            }
        }
    }

    fun saveCustomers(items: List<Customer>) {
        val arr = JSONArray()
        items.forEach {
            arr.put(
                JSONObject().apply {
                    put("name", it.name)
                    put("phone", it.phone)
                    put("gst", it.gst)
                }
            )
        }
        prefs.edit().putString(customersKey, arr.toString()).apply()
    }

    fun loadInvoiceHistory(): List<InvoiceRecord> {
        val raw = prefs.getString(invoiceHistoryKey, "[]") ?: "[]"
        val arr = JSONArray(raw)
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(
                    InvoiceRecord(
                        invoiceNumber = obj.getString("invoiceNumber"),
                        invoiceDate = obj.getString("invoiceDate"),
                        customerName = obj.getString("customerName"),
                        customerPhone = obj.optString("customerPhone", ""),
                        items = parseItems(obj.optJSONArray("items")),
                        shippingCharges = obj.optDouble("shippingCharges", 0.0),
                        total = obj.getDouble("total"),
                        filePath = obj.getString("filePath"),
                        createdAtMillis = obj.optLong("createdAtMillis", parseDateToMillis(obj.getString("invoiceDate"))),
                        paymentReceived = obj.optBoolean("paymentReceived", false)
                    )
                )
            }
        }
    }

    fun saveInvoiceHistory(items: List<InvoiceRecord>) {
        val arr = JSONArray()
        items.forEach {
            arr.put(
                JSONObject().apply {
                    put("invoiceNumber", it.invoiceNumber)
                    put("invoiceDate", it.invoiceDate)
                    put("customerName", it.customerName)
                    put("customerPhone", it.customerPhone)
                    put("items", itemsToJson(it.items))
                    put("shippingCharges", it.shippingCharges)
                    put("total", it.total)
                    put("filePath", it.filePath)
                    put("createdAtMillis", it.createdAtMillis)
                    put("paymentReceived", it.paymentReceived)
                }
            )
        }
        prefs.edit().putString(invoiceHistoryKey, arr.toString()).apply()
    }

    fun exportProductsJson(): String {
        return prefs.getString(productsKey, "[]") ?: "[]"
    }

    fun exportCustomersJson(): String {
        return prefs.getString(customersKey, "[]") ?: "[]"
    }

    fun previewInvoiceNumber(): String {
        val next = prefs.getInt(invoiceSequenceKey, 0) + 1
        val datePrefix = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())
        return "$datePrefix${next.toString().padStart(4, '0')}"
    }

    fun consumeInvoiceNumber(): String {
        val current = prefs.getInt(invoiceSequenceKey, 0) + 1
        prefs.edit().putInt(invoiceSequenceKey, current).apply()
        val datePrefix = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())
        return "$datePrefix${current.toString().padStart(4, '0')}"
    }

    private fun parseDateToMillis(dateText: String): Long {
        return try {
            val parser = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            parser.parse(dateText)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    private fun parseItems(arr: JSONArray?): List<InvoiceLineItem> {
        if (arr == null) return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(
                    InvoiceLineItem(
                        productName = obj.optString("productName", ""),
                        quantity = obj.optDouble("quantity", 0.0),
                        unitPrice = obj.optDouble("unitPrice", 0.0)
                    )
                )
            }
        }
    }

    private fun itemsToJson(items: List<InvoiceLineItem>): JSONArray {
        val arr = JSONArray()
        items.forEach {
            arr.put(
                JSONObject().apply {
                    put("productName", it.productName)
                    put("quantity", it.quantity)
                    put("unitPrice", it.unitPrice)
                }
            )
        }
        return arr
    }
}

object InvoicePdfGenerator {
    fun createInvoicePdf(context: Context, invoice: InvoiceData): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(1240, 1754, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val mutedLinePaint = Paint().apply {
                color = Color.parseColor("#D8D8D8")
                strokeWidth = 1.2f
            }
            val lightFillPaint = Paint().apply {
                color = Color.parseColor("#F7F8F2")
                style = Paint.Style.FILL
            }
            val whiteFillPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            val headingPaint = Paint().apply {
                color = Color.parseColor("#5E6840")
                textSize = 46f
                isFakeBoldText = true
            }
            val bodyPaint = Paint().apply {
                color = Color.parseColor("#111111")
                textSize = 27f
            }
            val strongPaint = Paint().apply {
                color = Color.parseColor("#111111")
                textSize = 30f
                isFakeBoldText = true
            }
            val brandPaint = Paint().apply {
                color = Color.parseColor("#5E6840")
                textSize = 54f
                isFakeBoldText = true
            }
            val smallPaint = Paint().apply {
                color = Color.parseColor("#555555")
                textSize = 22f
            }
            val contactPaint = Paint().apply {
                color = Color.parseColor("#111111")
                textSize = 23f
            }
            val metaPaint = Paint().apply {
                color = Color.parseColor("#444444")
                textSize = 22f
            }
            val tableHeaderPaint = Paint().apply {
                color = Color.parseColor("#1C1C1C")
                textSize = 27f
                isFakeBoldText = true
            }
            val bodyRightPaint = Paint(bodyPaint).apply {
                textAlign = Paint.Align.RIGHT
            }
            val strongRightPaint = Paint(strongPaint).apply {
                textAlign = Paint.Align.RIGHT
            }

            canvas.drawRect(40f, 40f, 1200f, 1714f, whiteFillPaint)
            canvas.drawRoundRect(RectF(40f, 40f, 1200f, 320f), 24f, 24f, lightFillPaint)

            val logoBitmapRaw = BitmapFactory.decodeResource(context.resources, R.drawable.brand_logo)
            val logoBitmap = removeBlackBackground(logoBitmapRaw)
            val logoRect = Rect(70, 78, 190, 198)
            val logoCircle = RectF(70f, 78f, 190f, 198f)
            canvas.drawOval(logoCircle, whiteFillPaint)
            canvas.save()
            canvas.clipRect(logoRect)
            canvas.drawBitmap(logoBitmap, null, logoRect, null)
            canvas.restore()

            canvas.drawText("Crumbs & Soul", 220f, 138f, brandPaint)
            canvas.drawText("Rooted Indulgence", 222f, 176f, smallPaint)
            // Header right: FSSAI logo + number aligned.
            val fssaiLogo = BitmapFactory.decodeResource(context.resources, R.drawable.fssai_logo)
            canvas.drawBitmap(fssaiLogo, null, Rect(740, 100, 830, 150), null)
            canvas.drawText("21225007001088", 840f, 138f, metaPaint)

            // Contact row with provided icons.
            val waIcon = BitmapFactory.decodeResource(context.resources, R.drawable.whatsapp_icon)
            val phoneIcon = BitmapFactory.decodeResource(context.resources, R.drawable.phone_icon)
            val instaIcon = BitmapFactory.decodeResource(context.resources, R.drawable.instagram_icon)
            canvas.drawBitmap(waIcon, null, Rect(220, 230, 246, 256), null)
            canvas.drawText("+91-9019508365", 256f, 252f, contactPaint)
            canvas.drawBitmap(phoneIcon, null, Rect(470, 230, 496, 256), null)
            canvas.drawText("+91-9962355820", 506f, 252f, contactPaint)
            canvas.drawBitmap(instaIcon, null, Rect(720, 230, 746, 256), null)
            canvas.drawText("@crumbs_and_soul", 756f, 252f, contactPaint)
            canvas.drawLine(60f, 308f, 1180f, 308f, mutedLinePaint)

            canvas.drawText("INVOICE", 70f, 380f, headingPaint)
            canvas.drawText("Invoice No: ${invoice.invoiceNumber}", 70f, 435f, strongPaint)
            canvas.drawText("Date: ${invoice.invoiceDate}", 860f, 435f, bodyPaint)
            canvas.drawText("Customer: ${invoice.customerName}", 70f, 485f, bodyPaint)
            if (invoice.customerPhone.isNotBlank()) {
                canvas.drawText("Phone: ${invoice.customerPhone}", 70f, 527f, bodyPaint)
            }
            canvas.drawText("UPI: hanajanalakshmi@okaxis", 70f, 575f, strongPaint)
            canvas.drawLine(60f, 604f, 1180f, 604f, mutedLinePaint)

            var y = 655f
            canvas.drawRoundRect(RectF(60f, 624f, 1180f, 680f), 8f, 8f, lightFillPaint)
            val colItemX = 75f
            val colQtyX = 665f
            val colUnitX = 805f
            val colLineHeaderX = 960f
            val amountRightX = 1135f
            canvas.drawText("Item", colItemX, y, tableHeaderPaint)
            canvas.drawText("Qty", colQtyX, y, tableHeaderPaint)
            canvas.drawText("Unit Price", colUnitX, y, tableHeaderPaint)
            canvas.drawText("Line Total", colLineHeaderX, y, tableHeaderPaint)
            canvas.drawLine(60f, 652f, 1180f, 652f, mutedLinePaint)
            y += 60f
            invoice.items.forEach {
                canvas.drawText(it.productName.take(28), 70f, y, bodyPaint)
                canvas.drawText(money(it.quantity), colQtyX + 15f, y, bodyPaint)
                canvas.drawText("₹${money(it.unitPrice)}", colUnitX - 5f, y, bodyPaint)
                canvas.drawText("₹${money(it.lineTotal())}", amountRightX, y, bodyRightPaint)
                canvas.drawLine(60f, y + 18f, 1180f, y + 18f, mutedLinePaint)
                y += 45f
            }

            y += 30f
            if (invoice.shippingCharges > 0.0) {
                canvas.drawRoundRect(RectF(720f, y - 28f, 1180f, y + 20f), 8f, 8f, lightFillPaint)
                canvas.drawText("Shipping Charges:", 740f, y + 5f, bodyPaint)
                canvas.drawText("₹${money(invoice.shippingCharges)}", amountRightX, y + 5f, bodyRightPaint)
                y += 62f
            }
            canvas.drawRoundRect(RectF(720f, y - 35f, 1180f, y + 25f), 8f, 8f, lightFillPaint)
            canvas.drawText("Total Amount:", 740f, y, strongPaint)
            canvas.drawText("₹${money(invoice.total)}", amountRightX, y, strongRightPaint)
            y += 85f
            canvas.drawLine(60f, y - 35f, 1180f, y - 35f, mutedLinePaint)
            canvas.drawText("Thank you for choosing Crumbs & Soul.", 70f, y, smallPaint)
            y += 34f
            canvas.drawText("This is a computer generated invoice. No signature required.", 70f, y, smallPaint)

            document.finishPage(page)
            val folder = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "CrumbsAndSoulInvoices"
            ).apply { mkdirs() }
            val pdfFile = File(folder, "Invoice_${invoice.invoiceNumber}.pdf")
            FileOutputStream(pdfFile).use { stream -> document.writeTo(stream) }
            document.close()
            pdfFile
        } catch (_: Exception) {
            null
        }
    }

    private fun removeBlackBackground(source: android.graphics.Bitmap): android.graphics.Bitmap {
        val out = source.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
        val width = out.width
        val height = out.height
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = out.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                if (r < 45 && g < 45 && b < 45) {
                    out.setPixel(x, y, Color.TRANSPARENT)
                }
            }
        }
        return out
    }
}

fun createSalesReportPdf(
    context: Context,
    reportType: String,
    monthFilter: String,
    customerFilter: String,
    rows: List<ReportRow>
): File? {
    return try {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(1240, 1754, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply {
            color = Color.parseColor("#5E6840")
            textSize = 44f
            isFakeBoldText = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#111111")
            textSize = 28f
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#111111")
            textSize = 26f
        }
        val boldPaint = Paint().apply {
            color = Color.parseColor("#111111")
            textSize = 28f
            isFakeBoldText = true
        }

        canvas.drawText("Crumbs & Soul - Sales Report", 70f, 100f, titlePaint)
        canvas.drawText("Type: $reportType", 70f, 150f, subtitlePaint)
        canvas.drawText("Month: $monthFilter | Customer: $customerFilter", 70f, 190f, subtitlePaint)
        canvas.drawLine(60f, 220f, 1180f, 220f, bodyPaint)

        var y = 270f
        canvas.drawText(if (reportType == "Month-wise") "Month" else "Customer", 70f, y, boldPaint)
        canvas.drawText("Invoices", 700f, y, boldPaint)
        canvas.drawText("Total Sales", 930f, y, boldPaint)
        y += 25f
        canvas.drawLine(60f, y, 1180f, y, bodyPaint)
        y += 40f

        rows.forEach {
            if (y > 1640f) return@forEach
            canvas.drawText(it.key.take(28), 70f, y, bodyPaint)
            canvas.drawText(it.invoiceCount.toString(), 730f, y, bodyPaint)
            canvas.drawText("₹${money(it.totalSales)}", 930f, y, bodyPaint)
            y += 42f
        }

        y += 20f
        canvas.drawLine(60f, y, 1180f, y, bodyPaint)
        y += 45f
        canvas.drawText(
            "Grand Total: ₹${money(rows.sumOf { it.totalSales })}",
            760f,
            y,
            boldPaint
        )
        y += 60f
        canvas.drawText(
            "Generated on: ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())}",
            70f,
            y,
            subtitlePaint
        )

        document.finishPage(page)
        val folder = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "CrumbsAndSoulReports"
        ).apply { mkdirs() }
        val file = File(folder, "SalesReport_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        file
    } catch (_: Exception) {
        null
    }
}

fun createSalesReportExcel(
    context: Context,
    reportType: String,
    monthFilter: String,
    customerFilter: String,
    rows: List<ReportRow>
): File? {
    return try {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Sales Report")
        var rowIndex = 0
        sheet.createRow(rowIndex++).apply { createCell(0).setCellValue("Crumbs & Soul Sales Report") }
        sheet.createRow(rowIndex++).apply { createCell(0).setCellValue("Type: $reportType") }
        sheet.createRow(rowIndex++).apply { createCell(0).setCellValue("Month: $monthFilter") }
        sheet.createRow(rowIndex++).apply { createCell(0).setCellValue("Customer: $customerFilter") }
        rowIndex++
        sheet.createRow(rowIndex++).apply {
            createCell(0).setCellValue(if (reportType == "Month-wise") "Month" else "Customer")
            createCell(1).setCellValue("Invoices")
            createCell(2).setCellValue("Total Sales")
        }
        rows.forEach {
            sheet.createRow(rowIndex++).apply {
                createCell(0).setCellValue(it.key)
                createCell(1).setCellValue(it.invoiceCount.toDouble())
                createCell(2).setCellValue(it.totalSales)
            }
        }
        sheet.createRow(rowIndex).apply {
            createCell(1).setCellValue("Grand Total")
            createCell(2).setCellValue(rows.sumOf { it.totalSales })
        }
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
        sheet.autoSizeColumn(2)

        val folder = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "CrumbsAndSoulReports"
        ).apply { mkdirs() }
        val file = File(folder, "SalesReport_${System.currentTimeMillis()}.xlsx")
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        file
    } catch (_: Exception) {
        null
    }
}

fun shareOnWhatsApp(context: Context, file: File, customerPhone: String) {
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val normalized = normalizeWhatsAppPhone(customerPhone)
    if (normalized == null) {
        Toast.makeText(context, "Invalid customer phone for WhatsApp", Toast.LENGTH_SHORT).show()
        return
    }
    val jid = "$normalized@s.whatsapp.net"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra("jid", jid)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        `package` = "com.whatsapp"
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        val businessIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra("jid", jid)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            `package` = "com.whatsapp.w4b"
        }
        try {
            context.startActivity(businessIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "WhatsApp not found. Opening share sheet.", Toast.LENGTH_SHORT).show()
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(fallback, "Share Invoice PDF"))
        }
    }
}

fun normalizeWhatsAppPhone(phone: String): String? {
    val digits = phone.filter { it.isDigit() }
    if (digits.length < 10) return null
    return when {
        digits.length == 10 -> "91$digits"
        digits.length == 12 && digits.startsWith("91") -> digits
        digits.length > 10 -> {
            val last10 = digits.takeLast(10)
            if (last10.length == 10) "91$last10" else null
        }
        else -> null
    }
}

fun formatPhoneForDisplay(normalizedDigits: String): String {
    return if (normalizedDigits.startsWith("91") && normalizedDigits.length >= 12) {
        normalizedDigits.takeLast(10)
    } else {
        normalizedDigits.takeLast(10)
    }
}

fun sharePdfFile(context: Context, file: File) {
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val fallback = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(fallback, "Share Invoice PDF"))
}

fun openPdfFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(viewIntent)
    } catch (_: Exception) {
        Toast.makeText(context, "No PDF viewer app found", Toast.LENGTH_SHORT).show()
    }
}

fun shareAnyFile(context: Context, file: File, mimeType: String) {
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Export Report"))
}

fun exportJsonAndShare(context: Context, fileName: String, json: String) {
    try {
        val backupDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "CrumbsAndSoulBackups"
        ).apply { mkdirs() }
        val backupFile = File(backupDir, fileName)
        backupFile.writeText(json)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Backup"))
    } catch (_: Exception) {
        Toast.makeText(context, "Failed to export backup", Toast.LENGTH_SHORT).show()
    }
}
