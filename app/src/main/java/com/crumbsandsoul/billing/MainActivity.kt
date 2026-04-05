package com.crumbsandsoul.billing

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.format.DateFormat
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.TimePicker
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class Product(val name: String, val defaultPrice: Double)
data class Customer(
    val name: String,
    val phone: String,
    val gst: String = "",
    val address: String = ""
)
data class InvoiceLineItem(var productName: String, var quantity: Double, var unitPrice: Double) {
    fun normalizedQuantity(): Int = quantity.roundToInt().coerceAtLeast(0)
    fun lineTotal(): Double = normalizedQuantity() * unitPrice
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
    var notes by mutableStateOf("")
    var invoiceType by mutableStateOf("B2C")
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
    val paymentReceived: Boolean,
    val invoiceType: String = "B2C",
    val notes: String = "",
    /** When true, PDF shows CANCELLED watermark; invoice number is retained. */
    val cancelled: Boolean = false
)

enum class AppSection(val title: String) {
    GenerateInvoice("Generate Invoice"),
    AddProduct("Product Details"),
    AddCustomer("Customer Details"),
    InvoiceHistory("Invoice History"),
    SalesReports("Reports"),
    Backup("Backup")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.argb(0xE6, 0xF7, 0xF4, 0xEA),
                Color.argb(0xE6, 0xF7, 0xF4, 0xEA)
            )
        )
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
    primaryContainer = ComposeColor(0xFFC5E8E2),
    onPrimaryContainer = ComposeColor(0xFF0F3D36),
    secondary = ComposeColor(0xFFBC9B56),
    onSecondary = ComposeColor(0xFF1B1B1B),
    secondaryContainer = ComposeColor(0xFFF2DFC4),
    onSecondaryContainer = ComposeColor(0xFF4A2C06),
    tertiary = ComposeColor(0xFF7A5A1E),
    onTertiary = ComposeColor(0xFFFFFFFF),
    tertiaryContainer = ComposeColor(0xFFE8D4B8),
    onTertiaryContainer = ComposeColor(0xFF3D2608),
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
    primaryContainer = ComposeColor(0xFF2D4A44),
    onPrimaryContainer = ComposeColor(0xFFB8E0D8),
    secondary = ComposeColor(0xFFD2B475),
    onSecondary = ComposeColor(0xFF2A2113),
    secondaryContainer = ComposeColor(0xFF4A3F28),
    onSecondaryContainer = ComposeColor(0xFFF0D9A8),
    tertiary = ComposeColor(0xFFE0B565),
    onTertiary = ComposeColor(0xFF221A08),
    tertiaryContainer = ComposeColor(0xFF5C4520),
    onTertiaryContainer = ComposeColor(0xFFF2DDB8),
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
    MaterialTheme(
        colorScheme = BrandLightColors,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingApp() {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val storage = remember { BillingStorage(context) }
    var section by remember { mutableStateOf(AppSection.GenerateInvoice) }
    val products = remember {
        mutableStateListOf<Product>().apply {
            addAll(storage.loadProducts().sortedBy { it.name.trim().lowercase(Locale.getDefault()) })
        }
    }
    val customers = remember {
        mutableStateListOf<Customer>().apply {
            addAll(storage.loadCustomers().sortedBy { it.name.trim().lowercase(Locale.getDefault()) })
        }
    }
    val invoiceHistory = remember { mutableStateListOf<InvoiceRecord>().apply { addAll(storage.loadInvoiceHistory()) } }
    val invoiceDraft = remember { InvoiceDraftState(storage.previewInvoiceNumber()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    LaunchedEffect(Unit) {
        AutoBackupScheduler.reschedule(context.applicationContext)
    }
    val invoiceHistoryRef = rememberUpdatedState(invoiceHistory)
    val productsRef = rememberUpdatedState(products)
    val customersRef = rememberUpdatedState(customers)

    val onSaveInvoicePdf = remember(context, storage, scope, snackbarHostState) {
        { invoice: InvoiceData, isUpdate: Boolean ->
            val ih0 = invoiceHistoryRef.value
            val existingForStamp = ih0.firstOrNull { it.invoiceNumber == invoice.invoiceNumber }
            val file = InvoicePdfGenerator.createInvoicePdf(
                context,
                invoice,
                stampCancelled = existingForStamp?.cancelled == true
            )
            if (file != null) {
                val ih = invoiceHistoryRef.value
                val existingIndex = ih.indexOfFirst { it.invoiceNumber == invoice.invoiceNumber }
                val existing = if (existingIndex >= 0) ih[existingIndex] else null
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
                    paymentReceived = existing?.paymentReceived ?: false,
                    invoiceType = normalizeInvoiceType(invoice.invoiceType),
                    notes = invoice.notes.trim(),
                    cancelled = existing?.cancelled ?: false
                )
                if (isUpdate && existingIndex >= 0) {
                    ih[existingIndex] = record
                } else {
                    ih.add(0, record)
                }
                storage.saveInvoiceHistory(ih.toList())
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = if (isUpdate) "Updated: ${file.name}" else "Saved: ${file.name}",
                        actionLabel = "Open",
                        withDismissAction = true
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        openPdfFile(context, file)
                    }
                }
                file
            } else {
                scope.launch { snackbarHostState.showSnackbar("Failed to save invoice PDF") }
                null
            }
        }
    }

    val onShareInvoiceWhatsapp = remember(context, storage, scope) {
        { invoice: InvoiceData, isUpdate: Boolean ->
            val ih0 = invoiceHistoryRef.value
            val existingForStamp = ih0.firstOrNull { it.invoiceNumber == invoice.invoiceNumber }
            val file = InvoicePdfGenerator.createInvoicePdf(
                context,
                invoice,
                stampCancelled = existingForStamp?.cancelled == true
            )
            if (file != null) {
                val ih = invoiceHistoryRef.value
                val existingIndex = ih.indexOfFirst { it.invoiceNumber == invoice.invoiceNumber }
                val existing = if (existingIndex >= 0) ih[existingIndex] else null
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
                    paymentReceived = existing?.paymentReceived ?: false,
                    invoiceType = normalizeInvoiceType(invoice.invoiceType),
                    notes = invoice.notes.trim(),
                    cancelled = existing?.cancelled ?: false
                )
                if (isUpdate && existingIndex >= 0) {
                    ih[existingIndex] = record
                } else {
                    ih.add(0, record)
                }
                storage.saveInvoiceHistory(ih.toList())
                shareInvoicePdfAndPaymentQrOnWhatsApp(context, file, invoice, invoice.customerPhone)
                file
            } else {
                scope.launch { snackbarHostState.showSnackbar("Failed to generate invoice PDF") }
                null
            }
        }
    }

    val onExportFullBackupStable = remember(context, storage) {
        {
            val zip = buildFullBackupZip(context, storage)
            if (zip != null) {
                shareAnyFile(
                    context,
                    zip,
                    "application/zip",
                    "Export full app backup"
                )
            } else {
                Toast.makeText(context, "Failed to create backup", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val onImportFullBackupStable = remember(context, storage, scope) {
        { uri: Uri ->
            scope.launch {
                val err = withContext(Dispatchers.IO) {
                    restoreFullBackupFromZip(context, storage, uri)
                }
                if (err != null) {
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                } else {
                    applyFullRestoreToUi(
                        context,
                        storage,
                        productsRef.value,
                        customersRef.value,
                        invoiceHistoryRef.value,
                        invoiceDraft
                    )
                }
            }
            Unit
        }
    }

    val onImportFullBackupFileStable = remember(context, storage, scope) {
        { file: File ->
            scope.launch {
                val err = withContext(Dispatchers.IO) {
                    restoreFullBackupFromZipFile(context, storage, file)
                }
                if (err != null) {
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                } else {
                    applyFullRestoreToUi(
                        context,
                        storage,
                        productsRef.value,
                        customersRef.value,
                        invoiceHistoryRef.value,
                        invoiceDraft
                    )
                }
            }
            Unit
        }
    }

    val resetDraftStable = remember(storage) {
        {
            invoiceDraft.selectedCustomerName = ""
            invoiceDraft.selectedCustomerPhone = ""
            invoiceDraft.customerSearch = ""
            invoiceDraft.productSearch = ""
            invoiceDraft.quantityInput = "1"
            invoiceDraft.priceInput = ""
            invoiceDraft.invoiceDate = ""
            invoiceDraft.editingInvoiceNumber = null
            invoiceDraft.shippingChargesInput = ""
            invoiceDraft.notes = ""
            invoiceDraft.invoiceType = "B2C"
            invoiceDraft.lineItems.clear()
            invoiceDraft.invoiceNumber = storage.previewInvoiceNumber()
        }
    }

    val previewInvoiceNumberStable = remember(storage) { { storage.previewInvoiceNumber() } }
    val consumeInvoiceNumberStable = remember(storage) { { storage.consumeInvoiceNumber() } }

    fun drawerNavLabel(destination: AppSection): String = when (destination) {
        AppSection.GenerateInvoice -> "Invoice"
        AppSection.AddProduct -> "Products"
        AppSection.AddCustomer -> "Customers"
        AppSection.InvoiceHistory -> "History"
        AppSection.SalesReports -> "Reports"
        AppSection.Backup -> "Backup"
    }

    fun drawerNavIcon(destination: AppSection) = when (destination) {
        AppSection.GenerateInvoice -> Icons.Default.Description
        AppSection.AddProduct -> Icons.Default.Category
        AppSection.AddCustomer -> Icons.Default.People
        AppSection.InvoiceHistory -> Icons.Default.History
        AppSection.SalesReports -> Icons.Default.Assessment
        AppSection.Backup -> Icons.Filled.Backup
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        // We dim and dismiss taps outside the sheet ourselves (see Box below) so taps reliably close the drawer.
        scrimColor = ComposeColor.Transparent,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(DrawerDefaults.MaximumDrawerWidth * 0.8f),
                drawerContainerColor = colorScheme.surface,
                drawerContentColor = colorScheme.onSurface
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Crumbs & Soul",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                )
                Text(
                    text = "Billing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 28.dp)
                        .padding(bottom = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                AppSection.entries.forEach { destination ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                drawerNavIcon(destination),
                                contentDescription = null
                            )
                        },
                        label = { Text(drawerNavLabel(destination)) },
                        selected = destination == section,
                        onClick = {
                            scope.launch { drawerState.close() }
                            section = destination
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = colorScheme.secondaryContainer,
                            selectedIconColor = ComposeColor(0xFF5E6840),
                            selectedTextColor = ComposeColor(0xFF5E6840)
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            ),
            topBar = {
                TopAppBar(
                    title = { Text(section.title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Open navigation menu"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.background,
                        titleContentColor = colorScheme.onBackground,
                        navigationIconContentColor = colorScheme.onBackground
                    )
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { padding ->
            Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // Final-pass Release (not tap detection): matches original UX for clearing focus on
                // taps that don’t register as a full gesture (e.g. some padding / scroll areas).
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Final)
                            if (event.type == PointerEventType.Release &&
                                event.changes.all { !it.isConsumed }
                            ) {
                                focusManager.clearFocus(force = true)
                            }
                        }
                    }
                }
                .background(ComposeColor(0xFFF7F4EA))
                .padding(horizontal = 16.dp)
                .padding(top = 0.dp, bottom = 12.dp)
            ) {
            when (section) {
                AppSection.GenerateInvoice -> InvoiceScreen(
                    products = products,
                    customers = customers,
                    draft = invoiceDraft,
                    onSavePdf = onSaveInvoicePdf,
                    onShareWhatsapp = onShareInvoiceWhatsapp,
                    previewInvoiceNumber = previewInvoiceNumberStable,
                    consumeInvoiceNumber = consumeInvoiceNumberStable,
                    resetDraft = resetDraftStable,
                    onAddCustomer = { c ->
                        customers.add(c)
                        customers.sortCustomersByNameAsc()
                        storage.saveCustomers(customers.toList())
                    },
                    onAddProduct = { p ->
                        products.add(p)
                        products.sortProductsByNameAsc()
                        storage.saveProducts(products.toList())
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
                        products.sortProductsByNameAsc()
                        storage.saveProducts(products.toList())
                    },
                    onUpdateProduct = { oldName, oldPrice, newName, newPrice ->
                        val idx = products.indexOfFirst {
                            it.name == oldName &&
                                it.defaultPrice.roundToInt() == oldPrice.roundToInt()
                        }
                        if (idx >= 0) {
                            products[idx] = Product(newName.trim(), newPrice)
                            products.sortProductsByNameAsc()
                            storage.saveProducts(products.toList())
                        }
                    },
                    onImport = { imported ->
                        products.clear()
                        products.addAll(imported)
                        products.sortProductsByNameAsc()
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
                    onAdd = { name, phone, gst, address ->
                        customers.add(Customer(name.trim(), phone.trim(), gst.trim(), address.trim()))
                        customers.sortCustomersByNameAsc()
                        storage.saveCustomers(customers.toList())
                    },
                    onUpdateCustomer = { oldPhone, newName, newPhone, newGst, newAddress ->
                        val idx = customers.indexOfFirst { it.phone == oldPhone }
                        if (idx >= 0) {
                            customers[idx] = Customer(
                                newName.trim(),
                                newPhone.trim(),
                                newGst.trim(),
                                newAddress.trim()
                            )
                            customers.sortCustomersByNameAsc()
                            storage.saveCustomers(customers.toList())
                        }
                    },
                    onDelete = { index ->
                        customers.removeAt(index)
                        storage.saveCustomers(customers.toList())
                    }
                )
                AppSection.InvoiceHistory -> InvoiceHistoryScreen(
                    records = invoiceHistory.toList(),
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
                    onCancelInvoice = { record ->
                        val index = invoiceHistory.indexOfFirst { it.invoiceNumber == record.invoiceNumber }
                        if (index >= 0) {
                            val newFile = replaceInvoicePdfForRecord(context, record, stampCancelled = true)
                            if (newFile == null) {
                                Toast.makeText(
                                    context,
                                    "Could not regenerate cancelled invoice PDF",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                invoiceHistory[index] = record.copy(cancelled = true, filePath = newFile.absolutePath)
                                storage.saveInvoiceHistory(invoiceHistory.toList())
                            }
                        }
                    },
                    onRestoreInvoice = { record ->
                        val index = invoiceHistory.indexOfFirst { it.invoiceNumber == record.invoiceNumber }
                        if (index >= 0) {
                            val newFile = replaceInvoicePdfForRecord(context, record, stampCancelled = false)
                            if (newFile == null) {
                                Toast.makeText(
                                    context,
                                    "Could not regenerate active invoice PDF",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                invoiceHistory[index] = record.copy(cancelled = false, filePath = newFile.absolutePath)
                                storage.saveInvoiceHistory(invoiceHistory.toList())
                            }
                        }
                    }
                )
                AppSection.SalesReports -> SalesReportScreen(
                    records = invoiceHistory,
                    customers = customers,
                    onReportExported = { file ->
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Saved: ${file.name}",
                                actionLabel = "Open",
                                withDismissAction = true
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                openExportedFile(context, file)
                            }
                        }
                    }
                )
                AppSection.Backup -> BackupScreen(
                    storage = storage,
                    onExportFullBackup = onExportFullBackupStable,
                    onImportFullBackup = onImportFullBackupStable,
                    onImportFullBackupFile = onImportFullBackupFileStable
                )
            }
            }
            }
            if (drawerState.isOpen) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(colorScheme.onSurface.copy(alpha = 0.38f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            scope.launch { drawerState.close() }
                        }
                )
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
    val total: Double,
    val invoiceType: String = "B2C",
    val notes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InvoiceScreen(
    products: List<Product>,
    customers: List<Customer>,
    draft: InvoiceDraftState,
    onSavePdf: (InvoiceData, Boolean) -> File?,
    onShareWhatsapp: (InvoiceData, Boolean) -> File?,
    previewInvoiceNumber: () -> String,
    consumeInvoiceNumber: () -> String,
    resetDraft: () -> Unit,
    onAddCustomer: (Customer) -> Unit,
    onAddProduct: (Product) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var selectedCustomer by remember {
        mutableStateOf(
            if (draft.selectedCustomerName.isNotBlank()) {
                Customer(draft.selectedCustomerName, draft.selectedCustomerPhone)
            } else null
        )
    }
    var customerSearch by remember { mutableStateOf(draft.customerSearch) }
    var customerSuggestionsVisible by remember { mutableStateOf(false) }
    var showQuickAddCustomer by remember { mutableStateOf(false) }
    /** When non-null, used as [QuickAddCustomerDialog] initial name instead of [customerSearch] (e.g. empty when + while a customer is selected). */
    var quickAddCustomerInitialOverride by remember { mutableStateOf<String?>(null) }
    val lineItems = draft.lineItems
    var productSearch by remember { mutableStateOf(draft.productSearch) }
    var productSuggestionsVisible by remember { mutableStateOf(false) }
    var showQuickAddProduct by remember { mutableStateOf(false) }
    var quantityInput by remember { mutableStateOf(draft.quantityInput) }
    var priceInput by remember { mutableStateOf(draft.priceInput) }
    var shippingChargesInput by remember { mutableStateOf(draft.shippingChargesInput) }
    var notesInput by remember { mutableStateOf(draft.notes) }
    var invoiceNumber by remember { mutableStateOf(draft.invoiceNumber.ifBlank { previewInvoiceNumber() }) }
    var editIndex by remember { mutableStateOf(-1) }
    var editName by remember { mutableStateOf("") }
    var editQty by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }
    var lastGeneratedFile by remember { mutableStateOf<File?>(null) }
    val today = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    var invoiceDate by remember { mutableStateOf(draft.invoiceDate.ifBlank { today }) }

    val filteredCustomers = remember(customers, customerSearch) {
        customers.filter {
            it.name.contains(customerSearch, ignoreCase = true) || it.phone.contains(customerSearch)
        }
    }
    val filteredProducts = remember(products, productSearch) {
        products.filter { it.name.contains(productSearch, ignoreCase = true) }
    }
    val subTotal = lineItems.sumOf { it.lineTotal() }
    val shippingCharges = (shippingChargesInput.toIntOrNull() ?: 0).toDouble()
    val total = subTotal + shippingCharges
    val isKeyboardVisible = WindowInsets.isImeVisible

    val brandLogoPainter = remember(context) {
        val raw = BitmapFactory.decodeResource(context.resources, R.drawable.brand_logo)
        val processed = removeBlackBackgroundFromLogo(raw)
        if (!raw.isRecycled) raw.recycle()
        BitmapPainter(processed.asImageBitmap())
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Crumbs & Soul", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Invoice No: $invoiceNumber", fontWeight = FontWeight.SemiBold)
                    Text("Date: $invoiceDate")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Invoice type", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        listOf("B2C", "B2B").forEach { type ->
                            FilterChip(
                                selected = normalizeInvoiceType(draft.invoiceType) == type,
                                onClick = { draft.invoiceType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                    if (draft.editingInvoiceNumber != null) {
                        Text("Editing Existing Invoice", color = ComposeColor(0xFF5E6840), fontWeight = FontWeight.SemiBold)
                    }
                }
                Image(
                    painter = brandLogoPainter,
                    contentDescription = "Crumbs & Soul logo",
                    modifier = Modifier
                        .size(72.dp)
                        .padding(start = 12.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = if (selectedCustomer != null) selectedCustomer!!.name else customerSearch,
        onValueChange = {
            if (selectedCustomer != null) {
                // Allow invoice-specific name edits while preserving selected customer's phone.
                selectedCustomer = selectedCustomer!!.copy(name = it)
                customerSearch = it
                customerSuggestionsVisible = false
            } else {
                selectedCustomer = null
                customerSearch = it
                customerSuggestionsVisible = true
            }
        },
        label = { Text("Search customer") },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        ),
        trailingIcon = {
            IconButton(
                onClick = {
                    customerSuggestionsVisible = false
                    quickAddCustomerInitialOverride = if (selectedCustomer != null) "" else null
                    showQuickAddCustomer = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add customer")
            }
        }
    )
    if (customerSuggestionsVisible && selectedCustomer == null && customerSearch.isNotBlank()) {
        val customerMatches = filteredCustomers.take(5)
        Card(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                customerMatches.forEach { customer ->
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
                if (customerMatches.isEmpty()) {
                    TextButton(
                        onClick = {
                            customerSuggestionsVisible = false
                            quickAddCustomerInitialOverride = null
                            showQuickAddCustomer = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add New Customer", fontWeight = FontWeight.SemiBold)
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
        ),
        trailingIcon = {
            IconButton(
                onClick = {
                    productSuggestionsVisible = false
                    showQuickAddProduct = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add product")
            }
        }
    )
    if (productSuggestionsVisible && productSearch.isNotBlank()) {
        val productMatches = filteredProducts.take(6)
        Card(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                productMatches.forEach { product ->
                    TextButton(
                        onClick = {
                            productSearch = product.name
                            priceInput = product.defaultPrice.roundToInt().toString()
                            productSuggestionsVisible = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(product.name, modifier = Modifier.fillMaxWidth())
                    }
                }
                if (productMatches.isEmpty()) {
                    TextButton(
                        onClick = {
                            productSuggestionsVisible = false
                            showQuickAddProduct = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add New Product", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = quantityInput,
            onValueChange = { quantityInput = it.filter { ch -> ch.isDigit() } },
            label = { Text("Quantity") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = priceInput,
            onValueChange = { priceInput = it.filter { ch -> ch.isDigit() } },
            label = { Text("Unit price") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
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
            val quantity = quantityInput.toIntOrNull()
            val price = priceInput.toIntOrNull()
            if (productSearch.isNotBlank() && quantity != null && quantity > 0 && price != null && price >= 0) {
                lineItems.add(InvoiceLineItem(productSearch.trim(), quantity.toDouble(), price.toDouble()))
                productSearch = ""
                productSuggestionsVisible = false
                quantityInput = "1"
                priceInput = ""
                focusManager.clearFocus()
                keyboardController?.hide()
                Toast.makeText(context, "Item added", Toast.LENGTH_SHORT).show()
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
                    Text(
                        text = item.productName,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Qty: ${item.normalizedQuantity()} x ₹${money(item.unitPrice)} = ₹${money(item.lineTotal())}",
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    editIndex = index
                                    editName = item.productName
                                    editQty = item.normalizedQuantity().toString()
                                    editPrice = item.unitPrice.roundToInt().toString()
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit line item")
                            }
                            IconButton(
                                onClick = { lineItems.removeAt(index) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete line item",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
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
        onValueChange = { shippingChargesInput = it.filter { ch -> ch.isDigit() } },
        label = { Text("Shipping Charges (Optional)") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        )
    )
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        value = notesInput,
        onValueChange = { notesInput = it },
        label = { Text("Notes (optional)") },
        placeholder = { Text("Shown on the invoice PDF") },
        minLines = 3,
        maxLines = 8,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Default
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
    if (!isKeyboardVisible) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                modifier = Modifier.weight(1f),
                enabled = lineItems.isNotEmpty(),
                onClick = {
                if (lineItems.isNotEmpty()) {
                    val invoice = InvoiceData(
                        invoiceNumber = invoiceNumber,
                        invoiceDate = invoiceDate,
                        customerName = selectedCustomer?.name ?: customerSearch.ifBlank { "Walk-in Customer" },
                        customerPhone = selectedCustomer?.phone ?: "",
                        items = lineItems.toList(),
                        shippingCharges = shippingCharges,
                        total = total,
                        invoiceType = normalizeInvoiceType(draft.invoiceType),
                        notes = notesInput
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
                    notesInput = ""
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            }
            ) { Text("Generate") }
            Button(
                modifier = Modifier.weight(1f),
                enabled = lineItems.isNotEmpty(),
                onClick = {
                if (lineItems.isNotEmpty()) {
                    val invoice = InvoiceData(
                        invoiceNumber = invoiceNumber,
                        invoiceDate = invoiceDate,
                        customerName = selectedCustomer?.name ?: customerSearch.ifBlank { "Walk-in Customer" },
                        customerPhone = selectedCustomer?.phone ?: "",
                        items = lineItems.toList(),
                        shippingCharges = shippingCharges,
                        total = total,
                        invoiceType = normalizeInvoiceType(draft.invoiceType),
                        notes = notesInput
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
                    notesInput = ""
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Generate &")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
            }
        }
    }
    }
    if (!isKeyboardVisible) {
        Spacer(modifier = Modifier.height(8.dp))
    }

    draft.selectedCustomerName = selectedCustomer?.name ?: ""
    draft.selectedCustomerPhone = selectedCustomer?.phone ?: ""
    draft.customerSearch = customerSearch
    draft.productSearch = productSearch
    draft.quantityInput = quantityInput
    draft.priceInput = priceInput
    draft.shippingChargesInput = shippingChargesInput
    draft.notes = notesInput
    draft.invoiceNumber = invoiceNumber
    draft.invoiceDate = invoiceDate

    QuickAddCustomerDialog(
        customers = customers,
        context = context,
        expanded = showQuickAddCustomer,
        initialName = quickAddCustomerInitialOverride ?: customerSearch,
        onDismiss = {
            showQuickAddCustomer = false
            quickAddCustomerInitialOverride = null
        },
        onCustomerSaved = { c ->
            onAddCustomer(c)
            selectedCustomer = c
            customerSearch = c.name
            customerSuggestionsVisible = false
        }
    )

    QuickAddProductDialog(
        products = products,
        expanded = showQuickAddProduct,
        initialName = productSearch,
        onDismiss = { showQuickAddProduct = false },
        onProductSaved = { p ->
            onAddProduct(p)
            productSearch = p.name
            priceInput = p.defaultPrice.roundToInt().toString()
            productSuggestionsVisible = false
        }
    )

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
                        onValueChange = { editQty = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Quantity") }
                    )
                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Unit price") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val qty = editQty.toIntOrNull()
                    val unit = editPrice.toIntOrNull()
                    if (qty != null && qty > 0 && unit != null && unit >= 0 && editName.isNotBlank() && editIndex in lineItems.indices) {
                        lineItems[editIndex] = InvoiceLineItem(editName.trim(), qty.toDouble(), unit.toDouble())
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
    onUpdateProduct: (oldName: String, oldPrice: Double, newName: String, newPrice: Double) -> Unit,
    onImport: (List<Product>) -> Unit,
    onDelete: (Int) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var pendingDeleteIndex by remember { mutableStateOf<Int?>(null) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var editProdName by remember { mutableStateOf("") }
    var editProdPrice by remember { mutableStateOf("") }
    var showImportConfirm by remember { mutableStateOf(false) }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (json.isNullOrBlank()) {
                    Toast.makeText(context, "Invalid backup file", Toast.LENGTH_SHORT).show()
                    return@rememberLauncherForActivityResult
                }
                val imported = BillingStorage(context).importProductsJson(json)
                onImport(imported)
                Toast.makeText(context, "Products backup imported", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(context, "Failed to import products backup", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Text("New product details", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val addPriceInt = price.toIntOrNull()
            val addNameNorm = name.trim().lowercase(Locale.getDefault())
            val addProductDuplicate =
                name.trim().isNotBlank() &&
                    products.any { it.name.trim().lowercase(Locale.getDefault()) == addNameNorm }
            val canSaveNewProduct =
                name.trim().isNotBlank() &&
                    addPriceInt != null &&
                    addPriceInt >= 0 &&
                    !addProductDuplicate
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                isError = addProductDuplicate,
                label = { Text("Product name *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            if (addProductDuplicate) {
                Text(
                    "Product with this name already exists",
                    color = ComposeColor(0xFFB00020),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                )
            }
            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { ch -> ch.isDigit() } },
                label = { Text("Default unit price *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canSaveNewProduct,
                onClick = {
                    val p = addPriceInt ?: return@Button
                    val trimmed = name.trim()
                    val normalized = trimmed.lowercase(Locale.getDefault())
                    if (products.any { it.name.trim().lowercase(Locale.getDefault()) == normalized }) {
                        Toast.makeText(context, "Duplicate product name not allowed", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onAdd(trimmed, p.toDouble())
                    name = ""
                    price = ""
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    Toast.makeText(context, "Product saved", Toast.LENGTH_SHORT).show()
                }
            ) { Text("Save Product") }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onExport,
            modifier = Modifier.weight(1f)
        ) { Text("Export Backup") }
        Button(
            onClick = { showImportConfirm = true },
            modifier = Modifier.weight(1f)
        ) { Text("Import Backup") }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("Saved product details", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn {
        itemsIndexed(
            products,
            key = { _, product -> product.name to product.defaultPrice }
        ) { index, product ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${product.name} - ₹${money(product.defaultPrice)}",
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    IconButton(
                        onClick = {
                            editingProduct = product
                            editProdName = product.name
                            editProdPrice = product.defaultPrice.roundToInt().toString()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit product")
                    }
                    IconButton(
                        onClick = { pendingDeleteIndex = index },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete product",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            HorizontalDivider()
        }
    }

    if (editingProduct != null) {
        val orig = editingProduct!!
        val editPriceInt = editProdPrice.toIntOrNull()
        val editNorm = editProdName.trim().lowercase(Locale.getDefault())
        val editProductNameTaken =
            editProdName.trim().isNotBlank() &&
                products.any {
                    it.name.trim().lowercase(Locale.getDefault()) == editNorm &&
                        !(it.name == orig.name &&
                            it.defaultPrice.roundToInt() == orig.defaultPrice.roundToInt())
                }
        val canSaveEditProduct =
            editProdName.trim().isNotBlank() &&
                editPriceInt != null &&
                editPriceInt >= 0 &&
                !editProductNameTaken
        AlertDialog(
            onDismissRequest = { editingProduct = null },
            title = { Text("Edit product details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editProdName,
                        onValueChange = { editProdName = it },
                        label = { Text("Product name *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = editProductNameTaken,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                    if (editProductNameTaken) {
                        Text(
                            "Another product already uses this name",
                            color = ComposeColor(0xFFB00020),
                            fontSize = 12.sp
                        )
                    }
                    OutlinedTextField(
                        value = editProdPrice,
                        onValueChange = { editProdPrice = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Default unit price *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = canSaveEditProduct,
                    onClick = {
                        val p = editPriceInt ?: return@TextButton
                        val trimmed = editProdName.trim()
                        val norm = trimmed.lowercase(Locale.getDefault())
                        val nameTaken = products.any {
                            it.name.trim().lowercase(Locale.getDefault()) == norm &&
                                !(it.name == orig.name &&
                                    it.defaultPrice.roundToInt() == orig.defaultPrice.roundToInt())
                        }
                        if (nameTaken) {
                            Toast.makeText(context, "Duplicate product name not allowed", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        onUpdateProduct(orig.name, orig.defaultPrice, trimmed, p.toDouble())
                        editingProduct = null
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        Toast.makeText(context, "Product updated", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingProduct = null }) { Text("Cancel") }
            }
        )
    }

    if (pendingDeleteIndex != null && pendingDeleteIndex in products.indices) {
        val idx = pendingDeleteIndex!!
        val product = products[idx]
        AlertDialog(
            onDismissRequest = { pendingDeleteIndex = null },
            title = { Text("Delete product details") },
            text = { Text("Delete product '${product.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(idx)
                    pendingDeleteIndex = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteIndex = null }) { Text("Cancel") }
            }
        )
    }

    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = { Text("Import product details backup") },
            text = { Text("This will replace your current products list. Continue?") },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    importLauncher.launch(arrayOf("application/json", "text/plain"))
                }) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

private fun copyPlainTextToClipboard(context: Context, clipLabel: String, text: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(clipLabel, text))
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}

/** WhatsApp often drops file attachments if [Intent.EXTRA_TEXT] is set — grant per-URI read as well. */
private fun grantReadToWhatsAppPackages(context: Context, uris: Collection<Uri>) {
    for (pkg in listOf("com.whatsapp", "com.whatsapp.w4b")) {
        for (uri in uris) {
            try {
                context.grantUriPermission(pkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
            }
        }
    }
}

private fun launchPhoneContactPickerForResult(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
    val pickIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
    launcher.launch(pickIntent)
}

/** Same fields as the Customers tab “Add customer” dialog; reusable from Generate Invoice search. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddCustomerDialog(
    customers: List<Customer>,
    context: Context,
    expanded: Boolean,
    initialName: String,
    onDismiss: () -> Unit,
    onCustomerSaved: (Customer) -> Unit
) {
    var addName by remember { mutableStateOf("") }
    var addPhone by remember { mutableStateOf("") }
    var addGst by remember { mutableStateOf("") }
    var addAddress by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val addScroll = rememberScrollState()

    LaunchedEffect(expanded) {
        if (expanded) {
            addName = initialName.trim()
            addPhone = ""
            addGst = ""
            addAddress = ""
        }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        try {
            context.contentResolver.query(
                uri,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val pickedName = if (nameIndex >= 0) cursor.getString(nameIndex).orEmpty() else ""
                    val pickedNumber = if (numberIndex >= 0) cursor.getString(numberIndex).orEmpty() else ""
                    val normalized = normalizeWhatsAppPhone(pickedNumber)
                    if (normalized == null) {
                        Toast.makeText(context, "Selected contact has invalid phone", Toast.LENGTH_SHORT).show()
                        return@use
                    }
                    addName = if (pickedName.isNotBlank()) pickedName else addName
                    addPhone = formatPhoneForDisplay(normalized)
                }
            }
        } catch (_: Exception) {
            Toast.makeText(context, "Failed to read selected contact", Toast.LENGTH_SHORT).show()
        }
    }
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchPhoneContactPickerForResult(contactPickerLauncher)
        } else {
            Toast.makeText(context, "Contacts permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val normalizedAddPhone = normalizeWhatsAppPhone(addPhone)
    val addPhoneDuplicate =
        normalizedAddPhone != null &&
            customers.any { normalizeWhatsAppPhone(it.phone) == normalizedAddPhone }
    val canSaveNewCustomer =
        addName.trim().isNotBlank() &&
            normalizedAddPhone != null &&
            !addPhoneDuplicate

    if (!expanded) return

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            focusManager.clearFocus()
            keyboardController?.hide()
        },
        title = { Text("Add customer") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(addScroll),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = addName,
                    onValueChange = { addName = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = addPhone,
                    onValueChange = { addPhone = it.filter { ch -> ch.isDigit() }.take(10) },
                    isError = addPhoneDuplicate,
                    label = { Text("Phone number *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )
                if (addPhoneDuplicate) {
                    Text(
                        "This phone number is already saved for another customer",
                        color = ComposeColor(0xFFB00020),
                        fontSize = 12.sp
                    )
                }
                OutlinedTextField(
                    value = addAddress,
                    onValueChange = { addAddress = it },
                    label = { Text("Address (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = addGst,
                    onValueChange = { addGst = it },
                    label = { Text("GST (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_CONTACTS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            launchPhoneContactPickerForResult(contactPickerLauncher)
                        } else {
                            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    }
                ) { Text("Pick from Contacts") }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSaveNewCustomer,
                onClick = {
                    val normalized = normalizeWhatsAppPhone(addPhone) ?: return@TextButton
                    if (customers.any { normalizeWhatsAppPhone(it.phone) == normalized }) {
                        Toast.makeText(context, "Duplicate phone number not allowed", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    val displayPhone = formatPhoneForDisplay(normalized)
                    onCustomerSaved(
                        Customer(
                            addName.trim(),
                            displayPhone,
                            addGst.trim(),
                            addAddress.trim()
                        )
                    )
                    onDismiss()
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    Toast.makeText(context, "Customer saved", Toast.LENGTH_SHORT).show()
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                focusManager.clearFocus()
                keyboardController?.hide()
            }) { Text("Cancel") }
        }
    )
}

@Composable
private fun QuickAddProductDialog(
    products: List<Product>,
    expanded: Boolean,
    initialName: String,
    onDismiss: () -> Unit,
    onProductSaved: (Product) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    LaunchedEffect(expanded) {
        if (expanded) {
            name = initialName.trim()
            price = ""
        }
    }

    val priceInt = price.toIntOrNull()
    val nameNorm = name.trim().lowercase(Locale.getDefault())
    val duplicate =
        name.trim().isNotBlank() &&
            products.any { it.name.trim().lowercase(Locale.getDefault()) == nameNorm }
    val canSave =
        name.trim().isNotBlank() &&
            priceInt != null &&
            priceInt >= 0 &&
            !duplicate

    if (!expanded) return

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            focusManager.clearFocus()
            keyboardController?.hide()
        },
        title = { Text("Add product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    isError = duplicate,
                    label = { Text("Product name *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                if (duplicate) {
                    Text(
                        "Product with this name already exists",
                        color = ComposeColor(0xFFB00020),
                        fontSize = 12.sp
                    )
                }
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Default unit price *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    val p = priceInt ?: return@TextButton
                    val trimmed = name.trim()
                    val normalized = trimmed.lowercase(Locale.getDefault())
                    if (products.any { it.name.trim().lowercase(Locale.getDefault()) == normalized }) {
                        Toast.makeText(context, "Duplicate product name not allowed", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    onProductSaved(Product(trimmed, p.toDouble()))
                    onDismiss()
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    Toast.makeText(context, "Product saved", Toast.LENGTH_SHORT).show()
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                focusManager.clearFocus()
                keyboardController?.hide()
            }) { Text("Cancel") }
        }
    )
}

@Composable
fun CustomerScreen(
    customers: List<Customer>,
    context: Context,
    onAdd: (String, String, String, String) -> Unit,
    onUpdateCustomer: (oldPhone: String, newName: String, newPhone: String, newGst: String, newAddress: String) -> Unit,
    onDelete: (Int) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var pendingDeleteIndex by remember { mutableStateOf<Int?>(null) }
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }
    var editCustName by remember { mutableStateOf("") }
    var editCustPhone by remember { mutableStateOf("") }
    var editCustGst by remember { mutableStateOf("") }
    var editCustAddress by remember { mutableStateOf("") }
    var viewCustomer by remember { mutableStateOf<Customer?>(null) }

    val editScroll = rememberScrollState()

    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            showAddCustomerDialog = true
        }
    ) {
        Text("Add New Customer")
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("Saved customer details", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn {
        itemsIndexed(
            customers,
            key = { _, customer -> customer.phone }
        ) { index, customer ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .clickable { viewCustomer = customer }
                ) {
                    Text(
                        text = customer.name,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = customer.phone,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    IconButton(
                        onClick = {
                            if (normalizeWhatsAppPhone(customer.phone) == null) {
                                Toast.makeText(
                                    context,
                                    "Add a valid 10-digit phone number to share the catalog",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                sendMenuCatalogWhatsApp(context, customer.phone, customer.name)
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share menu catalog on WhatsApp",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            editingCustomer = customer
                            editCustName = customer.name
                            editCustPhone = customer.phone
                            editCustGst = customer.gst
                            editCustAddress = customer.address
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit customer")
                    }
                    IconButton(
                        onClick = { pendingDeleteIndex = index },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete customer",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            HorizontalDivider()
        }
    }

    QuickAddCustomerDialog(
        customers = customers,
        context = context,
        expanded = showAddCustomerDialog,
        initialName = "",
        onDismiss = {
            showAddCustomerDialog = false
            focusManager.clearFocus()
            keyboardController?.hide()
        },
        onCustomerSaved = { c ->
            onAdd(c.name, c.phone, c.gst, c.address)
        }
    )

    if (editingCustomer != null) {
        val orig = editingCustomer!!
        val normalizedEditPhone = normalizeWhatsAppPhone(editCustPhone)
        val editIdx = customers.indexOfFirst { it.phone == orig.phone }
        val editPhoneDuplicate =
            normalizedEditPhone != null &&
                customers.withIndex().any { (i, c) ->
                    i != editIdx && normalizeWhatsAppPhone(c.phone) == normalizedEditPhone
                }
        val canSaveEditCustomer =
            editCustName.trim().isNotBlank() &&
                normalizedEditPhone != null &&
                !editPhoneDuplicate
        AlertDialog(
            onDismissRequest = { editingCustomer = null },
            title = { Text("Edit customer details") },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 480.dp)
                        .verticalScroll(editScroll),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editCustName,
                        onValueChange = { editCustName = it },
                        label = { Text("Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = editCustPhone,
                        onValueChange = { editCustPhone = it.filter { ch -> ch.isDigit() }.take(10) },
                        label = { Text("Phone (10 digits) *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = editPhoneDuplicate,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                    if (editPhoneDuplicate) {
                        Text(
                            "This phone number is already saved for another customer",
                            color = ComposeColor(0xFFB00020),
                            fontSize = 12.sp
                        )
                    }
                    OutlinedTextField(
                        value = editCustAddress,
                        onValueChange = { editCustAddress = it },
                        label = { Text("Address (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 5,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = editCustGst,
                        onValueChange = { editCustGst = it },
                        label = { Text("GST (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = canSaveEditCustomer,
                    onClick = {
                        val newNorm = normalizeWhatsAppPhone(editCustPhone) ?: return@TextButton
                        val idx = customers.indexOfFirst { it.phone == orig.phone }
                        val phoneTaken = customers.withIndex().any { (i, c) ->
                            i != idx && normalizeWhatsAppPhone(c.phone) == newNorm
                        }
                        if (phoneTaken) {
                            Toast.makeText(context, "Duplicate phone number not allowed", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val displayPhone = formatPhoneForDisplay(newNorm)
                        onUpdateCustomer(
                            orig.phone,
                            editCustName.trim(),
                            displayPhone,
                            editCustGst.trim(),
                            editCustAddress.trim()
                        )
                        editingCustomer = null
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        Toast.makeText(context, "Customer updated", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingCustomer = null }) { Text("Cancel") }
            }
        )
    }

    if (viewCustomer != null) {
        val c = viewCustomer!!
        AlertDialog(
            onDismissRequest = { viewCustomer = null },
            title = { Text("Customer details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                            Text(
                                "Name",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(c.name, fontSize = 16.sp)
                        }
                        IconButton(
                            onClick = {
                                copyPlainTextToClipboard(
                                    context,
                                    "Customer name",
                                    c.name,
                                    "Name copied"
                                )
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy name")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                            Text(
                                "Phone",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(c.phone, fontSize = 16.sp)
                        }
                        IconButton(
                            onClick = {
                                copyPlainTextToClipboard(
                                    context,
                                    "Customer phone",
                                    c.phone,
                                    "Phone copied"
                                )
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy phone")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                            Text(
                                "Address",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (c.address.isNotBlank()) c.address else "—",
                                fontSize = 16.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                copyPlainTextToClipboard(
                                    context,
                                    "Customer address",
                                    c.address.trim().ifBlank { "—" },
                                    "Address copied"
                                )
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy address")
                        }
                    }
                    if (c.gst.isNotBlank()) {
                        Text("GST", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(c.gst, fontSize = 16.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewCustomer = null }) { Text("Close") }
            }
        )
    }

    if (pendingDeleteIndex != null && pendingDeleteIndex in customers.indices) {
        val idx = pendingDeleteIndex!!
        val customer = customers[idx]
        AlertDialog(
            onDismissRequest = { pendingDeleteIndex = null },
            title = { Text("Delete customer details") },
            text = { Text("Delete customer '${customer.name}' (${customer.phone})?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(idx)
                    pendingDeleteIndex = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteIndex = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHistoryScreen(
    records: List<InvoiceRecord>,
    products: List<Product>,
    onUpdateStatus: (String, Boolean) -> Unit,
    onEditInvoice: (InvoiceRecord) -> Unit,
    onCancelInvoice: (InvoiceRecord) -> Unit,
    onRestoreInvoice: (InvoiceRecord) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var loadedDepth by remember { mutableStateOf<InvoiceHistoryLoadDepth>(InvoiceHistoryLoadDepth.LastDays(7)) }
    var historyLoading by remember { mutableStateOf(false) }
    val dateRangeOptions = remember {
        listOf("Last 7 Days", "Last 30 Days", "Last 90 Days", "All Time")
    }
    val paymentOptions = listOf("All", "Received", "Pending")
    val statusFilterOptions = listOf("All", "Active", "Cancelled")
    var customerFilter by remember { mutableStateOf("All Customers") }
    var dateFilter by remember { mutableStateOf("Last 7 Days") }
    var paymentFilter by remember { mutableStateOf(paymentOptions.first()) }
    var statusFilter by remember { mutableStateOf(statusFilterOptions.first()) }
    var customerExpanded by remember { mutableStateOf(false) }
    var dateExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var pendingCancel by remember { mutableStateOf<InvoiceRecord?>(null) }
    var pendingRestore by remember { mutableStateOf<InvoiceRecord?>(null) }
    var pendingPaymentReminder by remember { mutableStateOf<InvoiceRecord?>(null) }
    var pendingPaymentChange by remember { mutableStateOf<Pair<InvoiceRecord, Boolean>?>(null) }
    var shareMenuExpandedFor by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(dateFilter, records.size) {
        val required = requiredLoadDepthForDateFilter(dateFilter)
        if (loadDepthCovers(loadedDepth, required)) return@LaunchedEffect
        historyLoading = true
        try {
            withContext(Dispatchers.Default) {
                delay(150)
            }
            loadedDepth = mergeLoadDepth(loadedDepth, required)
        } finally {
            historyLoading = false
        }
    }
    val baseRecords = remember(records, loadedDepth) {
        when (val d = loadedDepth) {
            InvoiceHistoryLoadDepth.AllTime -> records
            is InvoiceHistoryLoadDepth.LastDays ->
                records.filter { isWithinDays(it.createdAtMillis, d.days) }
        }
    }
    val customerOptions = remember(baseRecords) {
        buildList {
            add("All Customers")
            addAll(baseRecords.map { it.customerName.ifBlank { "Walk-in Customer" } }.distinct().sorted())
        }
    }
    LaunchedEffect(customerOptions, customerFilter) {
        if (customerFilter !in customerOptions) customerFilter = "All Customers"
    }
    LaunchedEffect(dateRangeOptions, dateFilter) {
        if (dateFilter !in dateRangeOptions) dateFilter = "Last 7 Days"
    }
    LaunchedEffect(statusFilterOptions, statusFilter) {
        if (statusFilter !in statusFilterOptions) statusFilter = "All"
    }
    var editingRecord by remember { mutableStateOf<InvoiceRecord?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editShipping by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }
    var editInvoiceType by remember { mutableStateOf("B2C") }
    val editItems = remember { mutableStateListOf<InvoiceLineItem>() }
    val editQtyInputs = remember { mutableStateMapOf<Int, String>() }
    val editUnitInputs = remember { mutableStateMapOf<Int, String>() }
    var activeProductIndex by remember { mutableStateOf(-1) }
    var pendingFocusItemIndex by remember { mutableStateOf(-1) }
    var showSaveEditConfirm by remember { mutableStateOf(false) }

    fun saveEditedInvoice(): Boolean {
        val record = editingRecord ?: return false
        val shipping = (editShipping.toIntOrNull() ?: 0).toDouble()
        val hasInvalidItems = editItems.isEmpty() || editItems.any {
            it.productName.isBlank() || it.normalizedQuantity() <= 0 || it.unitPrice < 0.0
        }
        if (hasInvalidItems) {
            Toast.makeText(context, "Every line item must have product, quantity > 0, and valid price", Toast.LENGTH_SHORT).show()
            return false
        }
        val sanitizedItems = editItems.map {
            InvoiceLineItem(
                it.productName.trim(),
                it.normalizedQuantity().toDouble(),
                it.unitPrice.roundToInt().coerceAtLeast(0).toDouble()
            )
        }
        val normalizedPhone = if (editPhone.isBlank()) "" else (normalizeWhatsAppPhone(editPhone) ?: run {
            Toast.makeText(context, "Enter valid 10-digit phone number", Toast.LENGTH_SHORT).show()
            return false
        })
        val displayPhone = if (normalizedPhone.isNotBlank()) formatPhoneForDisplay(normalizedPhone) else ""
        val newTotal = sanitizedItems.sumOf { it.lineTotal() } + shipping
        val updated = record.copy(
            customerName = editName.ifBlank { record.customerName },
            customerPhone = displayPhone,
            items = sanitizedItems,
            shippingCharges = shipping,
            total = newTotal,
            invoiceType = normalizeInvoiceType(editInvoiceType),
            notes = editNotes.trim()
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
                total = updated.total,
                invoiceType = updated.invoiceType,
                notes = updated.notes
            ),
            stampCancelled = record.cancelled
        )
        if (newPdf == null) {
            Toast.makeText(context, "Failed to regenerate updated invoice PDF", Toast.LENGTH_SHORT).show()
            return false
        }

        // Persist updated invoice to a unique file to avoid stale viewer cache on same URI/path.
        val oldPdf = File(record.filePath)
        val updatedFile = File(
            newPdf.parentFile ?: oldPdf.parentFile ?: context.filesDir,
            "Invoice_${record.invoiceNumber}_${System.currentTimeMillis()}.pdf"
        )
        val finalFile = try {
            if (newPdf.absolutePath == updatedFile.absolutePath) {
                newPdf
            } else {
                newPdf.copyTo(updatedFile, overwrite = true)
            }
        } catch (_: Exception) {
            newPdf
        }

        // Remove temporary generated file if we copied to a different unique file.
        if (newPdf.absolutePath != finalFile.absolutePath && newPdf.exists()) {
            newPdf.delete()
        }

        // First switch record to the new updated invoice file.
        onEditInvoice(updated.copy(filePath = finalFile.absolutePath))

        // Finally delete old invoice file if it is different.
        if (oldPdf.exists() && oldPdf.absolutePath != finalFile.absolutePath) {
            val oldDeleted = oldPdf.delete()
            if (!oldDeleted) {
                Toast.makeText(context, "Invoice updated, but old invoice deletion failed", Toast.LENGTH_SHORT).show()
                editingRecord = null
                showSaveEditConfirm = false
                return true
            }
        }

        Toast.makeText(context, "Invoice updated", Toast.LENGTH_SHORT).show()
        editingRecord = null
        showSaveEditConfirm = false
        return true
    }

    val filtered = remember(baseRecords, customerFilter, paymentFilter, dateFilter, statusFilter) {
        baseRecords.filter { record ->
            val customerMatch =
                customerFilter == "All Customers" || record.customerName.ifBlank { "Walk-in Customer" } == customerFilter
            val paymentMatch = when (paymentFilter) {
                "Received" -> record.paymentReceived
                "Pending" -> !record.paymentReceived
                else -> true
            }
            val dateMatch = when (dateFilter) {
                "Last 7 Days" -> isWithinDays(record.createdAtMillis, 7)
                "Last 30 Days" -> isWithinDays(record.createdAtMillis, 30)
                "Last 90 Days" -> isWithinDays(record.createdAtMillis, 90)
                "All Time" -> true
                else -> isWithinDays(record.createdAtMillis, 7)
            }
            val statusMatch = when (statusFilter) {
                "Active" -> !record.cancelled
                "Cancelled" -> record.cancelled
                else -> true
            }
            customerMatch && paymentMatch && dateMatch && statusMatch
        }
    }

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
    DropdownField(
        label = "Invoice status",
        value = statusFilter,
        options = statusFilterOptions,
        expanded = statusExpanded,
        onExpanded = { statusExpanded = it },
        onSelect = { statusFilter = it }
    )

    if (historyLoading) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (records.isEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text("No invoices yet.")
    } else if (baseRecords.isEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text("No invoices in the loaded period. Choose a wider date filter to load more.")
    } else if (filtered.isEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text("No invoices match your filters.")
    } else {
        LazyColumn {
        itemsIndexed(
            filtered,
            key = { _, record -> record.invoiceNumber }
        ) { _, record ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    val paymentEnabled = !record.cancelled
                    val tileColor = when {
                        !paymentEnabled -> ComposeColor(0xFFF0F0F0)
                        record.paymentReceived -> ComposeColor(0xFFE9F8EC)
                        else -> ComposeColor(0xFFFFEBEB)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(tileColor, RoundedCornerShape(10.dp))
                            .then(
                                if (paymentEnabled) {
                                    Modifier.clickable {
                                        pendingPaymentChange = record to !record.paymentReceived
                                    }
                                } else {
                                    Modifier
                                }
                            )
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            when {
                                record.cancelled -> "Cancelled — payment status frozen"
                                record.paymentReceived -> "Payment Received"
                                else -> "Payment Pending"
                            },
                            color = when {
                                record.cancelled -> ComposeColor(0xFF616161)
                                record.paymentReceived -> ComposeColor(0xFF1E7E34)
                                else -> ComposeColor(0xFFB00020)
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = {
                                if (paymentEnabled) pendingPaymentChange = record to !record.paymentReceived
                            },
                            enabled = paymentEnabled
                        ) {
                            Icon(
                                if (record.paymentReceived) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                contentDescription = "Toggle payment status"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Invoice: ${record.invoiceNumber}", fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val typeLabel = normalizeInvoiceType(record.invoiceType)
                            Text(
                                typeLabel,
                                modifier = Modifier
                                    .background(
                                        if (typeLabel == "B2B") ComposeColor(0xFFE3F2FD) else ComposeColor(0xFFFFF3E0),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (typeLabel == "B2B") ComposeColor(0xFF1565C0) else ComposeColor(0xFFE65100)
                            )
                            val statusLabel = invoiceRecordStatusLabel(record.cancelled)
                            Text(
                                statusLabel,
                                modifier = Modifier
                                    .background(
                                        if (record.cancelled) ComposeColor(0xFFFFEBEE) else ComposeColor(0xFFE8F5E9),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (record.cancelled) ComposeColor(0xFFC62828) else ComposeColor(0xFF2E7D32)
                            )
                        }
                    }
                    Text("Date: ${record.invoiceDate}")
                    Text("Customer: ${record.customerName}")
                    Text("Total: ₹${money(record.total)}")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.End)
                    ) {
                        IconButton(
                            onClick = {
                                editingRecord = record
                                editInvoiceType = normalizeInvoiceType(record.invoiceType)
                                editName = record.customerName
                                editPhone = record.customerPhone
                                editShipping = if (record.shippingCharges > 0.0) record.shippingCharges.roundToInt().toString() else ""
                                editNotes = record.notes
                                editItems.clear()
                                editItems.addAll(record.items.map { InvoiceLineItem(it.productName, it.quantity, it.unitPrice) })
                                editQtyInputs.clear()
                                editUnitInputs.clear()
                                editItems.forEachIndexed { idx, item ->
                                    editQtyInputs[idx] = item.normalizedQuantity().toString()
                                    editUnitInputs[idx] = item.unitPrice.roundToInt().toString()
                                }
                            },
                            modifier = Modifier.size(48.dp)
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
                        }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Visibility, contentDescription = "View invoice")
                        }
                        Box {
                            IconButton(
                                onClick = {
                                    shareMenuExpandedFor =
                                        if (shareMenuExpandedFor == record.invoiceNumber) null else record.invoiceNumber
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share")
                            }
                            DropdownMenu(
                                expanded = shareMenuExpandedFor == record.invoiceNumber,
                                onDismissRequest = { shareMenuExpandedFor = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Share invoice") },
                                    onClick = {
                                        shareMenuExpandedFor = null
                                        val file = File(record.filePath)
                                        if (file.exists()) {
                                            if (record.customerPhone.isNotBlank()) {
                                                shareOnWhatsApp(context, file, record.customerPhone)
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Customer phone missing for WhatsApp share",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "PDF not found on device", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share payment QR") },
                                    onClick = {
                                        shareMenuExpandedFor = null
                                        if (record.customerPhone.isBlank()) {
                                            Toast.makeText(
                                                context,
                                                "Customer phone missing for WhatsApp",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (normalizeWhatsAppPhone(record.customerPhone) == null) {
                                            Toast.makeText(
                                                context,
                                                "Invalid phone number for WhatsApp",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            sharePaymentQrOnlyOnWhatsApp(
                                                context,
                                                invoiceDataFromRecord(record),
                                                record.customerPhone
                                            )
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Google review") },
                                    onClick = {
                                        shareMenuExpandedFor = null
                                        if (record.customerPhone.isBlank()) {
                                            Toast.makeText(
                                                context,
                                                "Add customer phone on this invoice to message on WhatsApp",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (normalizeWhatsAppPhone(record.customerPhone) == null) {
                                            Toast.makeText(
                                                context,
                                                "Invalid phone number for WhatsApp",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            sendGoogleReviewWhatsApp(context, record.customerPhone)
                                        }
                                    }
                                )
                            }
                        }
                        if (!record.paymentReceived && !record.cancelled) {
                            IconButton(
                                onClick = {
                                    if (record.customerPhone.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Add customer phone on this invoice to send a reminder",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (normalizeWhatsAppPhone(record.customerPhone) == null) {
                                        Toast.makeText(
                                            context,
                                            "Invalid phone number for WhatsApp",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        pendingPaymentReminder = record
                                    }
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send payment reminder via WhatsApp"
                                )
                            }
                        }
                        if (!record.cancelled) {
                            IconButton(onClick = { pendingCancel = record }, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    Icons.Default.Block,
                                    contentDescription = "Cancel invoice",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            IconButton(onClick = { pendingRestore = record }, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Restore invoice to active",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }

    if (pendingPaymentChange != null) {
        val (payRecord, newReceived) = pendingPaymentChange!!
        AlertDialog(
            onDismissRequest = { pendingPaymentChange = null },
            title = { Text("Change payment status?") },
            text = {
                Text(
                    if (newReceived) {
                        "Mark invoice ${payRecord.invoiceNumber} as payment received?"
                    } else {
                        "Mark invoice ${payRecord.invoiceNumber} as payment pending?"
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateStatus(payRecord.invoiceNumber, newReceived)
                    pendingPaymentChange = null
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { pendingPaymentChange = null }) { Text("Cancel") }
            }
        )
    }

    if (pendingPaymentReminder != null) {
        val reminderRecord = pendingPaymentReminder!!
        AlertDialog(
            onDismissRequest = { pendingPaymentReminder = null },
            title = { Text("Send payment reminder?") },
            text = {
                Text(
                    "Open WhatsApp to send a payment reminder for invoice ${reminderRecord.invoiceNumber} " +
                        "(${reminderRecord.invoiceDate}) — ₹${money(reminderRecord.total)} to " +
                        reminderRecord.customerName.trim().ifBlank { "customer" } + "?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    sendPaymentReminderWhatsApp(
                        context,
                        reminderRecord.customerPhone,
                        reminderRecord.invoiceNumber,
                        reminderRecord.invoiceDate,
                        money(reminderRecord.total)
                    )
                    pendingPaymentReminder = null
                }) { Text("Send") }
            },
            dismissButton = {
                TextButton(onClick = { pendingPaymentReminder = null }) { Text("Cancel") }
            }
        )
    }

    if (pendingCancel != null) {
        val c = pendingCancel!!
        AlertDialog(
            onDismissRequest = { pendingCancel = null },
            title = { Text("Cancel invoice?") },
            text = {
                Text(
                    "Invoice ${c.invoiceNumber} will stay in history with the same number. " +
                        "A new PDF will be generated with CANCELLED marked on it. Summary sales totals exclude cancelled invoices."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onCancelInvoice(c)
                    pendingCancel = null
                }) { Text("Cancel invoice") }
            },
            dismissButton = {
                TextButton(onClick = { pendingCancel = null }) { Text("Back") }
            }
        )
    }

    if (pendingRestore != null) {
        val r = pendingRestore!!
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("Restore invoice?") },
            text = {
                Text(
                    "Invoice ${r.invoiceNumber} will be marked Active again and the PDF will be regenerated without the cancelled stamp."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRestoreInvoice(r)
                    pendingRestore = null
                }) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) { Text("Back") }
            }
        )
    }

    if (editingRecord != null) {
        AlertDialog(
            onDismissRequest = {
                focusManager.clearFocus()
                keyboardController?.hide()
                editingRecord = null
                showSaveEditConfirm = false
            },
            title = { Text("Edit Invoice") },
            text = {
                val dialogScrollState = rememberScrollState()
                LaunchedEffect(pendingFocusItemIndex, editItems.size) {
                    if (pendingFocusItemIndex >= 0) {
                        // Bring newly added item into view before requesting field focus.
                        delay(100)
                        dialogScrollState.animateScrollTo(dialogScrollState.maxValue)
                    }
                }
                // Final pass: clear focus when tap isn't consumed (e.g. labels, padding). TextFields consume taps.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Final)
                                    if (event.type == PointerEventType.Release &&
                                        event.changes.all { !it.isConsumed }
                                    ) {
                                        focusManager.clearFocus(force = true)
                                        keyboardController?.hide()
                                    }
                                }
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(dialogScrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                        onValueChange = { editShipping = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Shipping Charges") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Notes (optional)") },
                        minLines = 2,
                        maxLines = 6,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                    Text("Invoice type", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("B2C", "B2B").forEach { type ->
                            FilterChip(
                                selected = normalizeInvoiceType(editInvoiceType) == type,
                                onClick = { editInvoiceType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                    Text(
                        "Leave shipping empty or 0 to remove it.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text("Line Items", fontWeight = FontWeight.SemiBold)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        editItems.forEachIndexed { index, item ->
                            val productFocusRequester = remember { FocusRequester() }
                            LaunchedEffect(pendingFocusItemIndex) {
                                if (pendingFocusItemIndex == index) {
                                    // Delay a frame so the new text field is in composition before requesting focus.
                                    delay(120)
                                    productFocusRequester.requestFocus()
                                    keyboardController?.show()
                                    pendingFocusItemIndex = -1
                                }
                            }
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = item.productName,
                                        onValueChange = {
                                            editItems[index] = item.copy(productName = it)
                                            activeProductIndex = index
                                        },
                                        label = { Text("Product") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(productFocusRequester)
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
                                                            editItems[index] = item.copy(
                                                                productName = p.name,
                                                                unitPrice = p.defaultPrice.roundToInt().toDouble()
                                                            )
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
                                            value = editQtyInputs[index] ?: item.normalizedQuantity().toString(),
                                            onValueChange = { value ->
                                                val digitsOnly = value.filter { ch -> ch.isDigit() }
                                                editQtyInputs[index] = digitsOnly
                                                if (digitsOnly.isEmpty()) {
                                                    editItems[index] = item.copy(quantity = 0.0)
                                                } else {
                                                    val qty = digitsOnly.toIntOrNull()
                                                    if (qty != null && qty > 0) {
                                                        editItems[index] = item.copy(quantity = qty.toDouble())
                                                    }
                                                }
                                            },
                                            label = { Text("Qty") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                        OutlinedTextField(
                                            value = editUnitInputs[index] ?: item.unitPrice.roundToInt().toString(),
                                            onValueChange = { value ->
                                                val digitsOnly = value.filter { ch -> ch.isDigit() }
                                                editUnitInputs[index] = digitsOnly
                                                if (digitsOnly.isEmpty()) {
                                                    editItems[index] = item.copy(unitPrice = 0.0)
                                                } else {
                                                    val price = digitsOnly.toIntOrNull()
                                                    if (price != null && price >= 0) {
                                                        editItems[index] = item.copy(unitPrice = price.toDouble())
                                                    }
                                                }
                                            },
                                            label = { Text("Unit Price") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                        IconButton(
                                            onClick = {
                                                editItems.removeAt(index)
                                                editQtyInputs.clear()
                                                editUnitInputs.clear()
                                                editItems.forEachIndexed { idx, current ->
                                                    editQtyInputs[idx] = current.normalizedQuantity().toString()
                                                    editUnitInputs[idx] = current.unitPrice.roundToInt().toString()
                                                }
                                            },
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Remove item",
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Button(onClick = {
                        editItems.add(InvoiceLineItem(productName = "", quantity = 1.0, unitPrice = 0.0))
                        editQtyInputs[editItems.lastIndex] = "1"
                        editUnitInputs[editItems.lastIndex] = "0"
                        pendingFocusItemIndex = editItems.lastIndex
                    }) {
                        Text("Add Item")
                    }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showSaveEditConfirm = true
                }) { Text("Save") }
            },
            dismissButton = {
                Button(onClick = {
                    editingRecord = null
                    showSaveEditConfirm = false
                }) { Text("Cancel") }
            }
        )
    }

    if (showSaveEditConfirm && editingRecord != null) {
        AlertDialog(
            onDismissRequest = { showSaveEditConfirm = false },
            title = { Text("Confirm Invoice Update") },
            text = { Text("Save the changes made to this invoice?") },
            confirmButton = {
                TextButton(onClick = { saveEditedInvoice() }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveEditConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

data class ReportRow(val key: String, val invoiceCount: Int, val totalSales: Double)
data class ReportDetailRow(
    val invoiceNumber: String,
    val invoiceType: String,
    val invoiceDate: String,
    val customerName: String,
    val mappedCustomerName: String,
    val customerPhone: String,
    val itemName: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double,
    val shippingCharges: Double,
    val invoiceTotal: Double,
    val paymentStatus: String,
    val invoiceStatus: String
)

data class ReportInvoicePdfRow(
    val invoiceNumber: String,
    val invoiceType: String,
    val invoiceStatus: String,
    val customerName: String,
    val invoiceDate: String,
    val totalItemQty: Int,
    val shippingCharges: Double,
    val invoiceTotal: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    storage: BillingStorage,
    onExportFullBackup: () -> Unit,
    onImportFullBackup: (Uri) -> Unit,
    onImportFullBackupFile: (File) -> Unit
) {
    val context = LocalContext.current
    val importScope = rememberCoroutineScope()
    var showImportBackupDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var pendingRestoreFile by remember { mutableStateOf<File?>(null) }
    var scannedBackupFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    val backupScanDateFormat = remember {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    }
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        showImportBackupDialog = false
        if (uri != null) {
            pendingRestoreUri = uri
            pendingRestoreFile = null
            showRestoreConfirmDialog = true
        }
    }
    LaunchedEffect(showImportBackupDialog) {
        if (showImportBackupDialog) {
            scannedBackupFiles = withContext(Dispatchers.IO) {
                scanBackupZipFiles(context.applicationContext)
            }
        }
    }
    val postNotificationsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                context,
                "Allow notifications to see when automatic backups succeed or fail.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    val pageScrollState = rememberScrollState()
    val importBackupDialogScrollState = rememberScrollState()
    val autoBackupFrequencyOptions = remember {
        listOf(
            "Daily" to 1,
            "Every 7 days" to 7,
            "Every 14 days" to 14,
            "Every 30 days" to 30
        )
    }
    var autoBackupEnabled by remember { mutableStateOf(false) }
    var autoBackupFreqLabel by remember { mutableStateOf(autoBackupFrequencyOptions.first().first) }
    var autoBackupFreqExpanded by remember { mutableStateOf(false) }
    var autoBackupHour by remember { mutableIntStateOf(0) }
    var autoBackupMinute by remember { mutableIntStateOf(0) }
    var showAutoBackupTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        autoBackupEnabled = storage.isAutoBackupEnabled()
        val days = storage.getAutoBackupIntervalDays()
        autoBackupFreqLabel = autoBackupFrequencyOptions.firstOrNull { it.second == days }?.first
            ?: autoBackupFrequencyOptions.first().first
        autoBackupHour = storage.getAutoBackupHour()
        autoBackupMinute = storage.getAutoBackupMinute()
        AutoBackupNotifications.ensureChannel(context.applicationContext)
    }
    // Ask for POST_NOTIFICATIONS whenever this screen is shown with auto backup on (not only on first toggle).
    LaunchedEffect(autoBackupEnabled) {
        if (autoBackupEnabled && Build.VERSION.SDK_INT >= 33) {
            val perm = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                postNotificationsLauncher.launch(perm)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(pageScrollState)
            .padding(bottom = 24.dp)
    ) {
        Text(
            "Export all products, customers, invoice history, and PDFs as a single ZIP archive. " +
                "Share or save it (Drive, email, Files, etc.). On a new phone, install the app and use Import to restore."
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Import replaces all products, customers, and invoice history on this device and " +
                "rebuilds invoice PDFs from the backup. You can tap the latest backup below or choose any .zip file.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))
        Text("Automatic backup", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Runs on your schedule (device local time). Only one file is kept; each run replaces " +
                AutoBackupScheduler.autoBackupZipFile(context).name + " in app storage. " +
                "You’ll get a notification after each run (success or failure) if notifications are allowed.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (autoBackupEnabled && !AutoBackupNotifications.canShowNotifications(context)) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Notifications are off for this app. Scheduled backups still run, but you won’t get success or failure alerts. Turn them on in system settings.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.error
            )
            TextButton(
                onClick = {
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                        )
                    } catch (_: Exception) {
                        Toast.makeText(
                            context,
                            "Open Settings → Apps → Notifications for this app",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier.padding(top = 4.dp)
            ) { Text("Open notification settings") }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable automatic backup", modifier = Modifier.weight(1f).padding(end = 8.dp))
            Switch(
                checked = autoBackupEnabled,
                onCheckedChange = { on ->
                    autoBackupEnabled = on
                    storage.setAutoBackupEnabled(on)
                    if (on) {
                        if (Build.VERSION.SDK_INT >= 33) {
                            val perm = Manifest.permission.POST_NOTIFICATIONS
                            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                                postNotificationsLauncher.launch(perm)
                            }
                        }
                    }
                    AutoBackupScheduler.reschedule(context.applicationContext)
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownField(
            label = "Backup frequency",
            value = autoBackupFreqLabel,
            options = autoBackupFrequencyOptions.map { it.first },
            expanded = autoBackupFreqExpanded,
            onExpanded = { autoBackupFreqExpanded = it },
            onSelect = { label ->
                autoBackupFreqLabel = label
                val days = autoBackupFrequencyOptions.firstOrNull { it.first == label }?.second ?: 1
                storage.setAutoBackupIntervalDays(days)
                AutoBackupScheduler.reschedule(context.applicationContext)
                autoBackupFreqExpanded = false
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text("Backup time", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = { showAutoBackupTimePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                formatAutoBackupTimeLabel(context, autoBackupHour, autoBackupMinute),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        Text(
            "Uses the device’s time zone. Longer intervals run on this clock time every N days.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        val backupActionBtnHeight = 72.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(backupActionBtnHeight),
                onClick = { onExportFullBackup() }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Export backup",
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(backupActionBtnHeight),
                onClick = { showImportBackupDialog = true }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Import backup",
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    if (showAutoBackupTimePicker) {
        val is24h = DateFormat.is24HourFormat(context)
        key(autoBackupHour, autoBackupMinute) {
            val timePickerState = rememberTimePickerState(
                initialHour = autoBackupHour,
                initialMinute = autoBackupMinute,
                is24Hour = is24h
            )
            AlertDialog(
                onDismissRequest = { showAutoBackupTimePicker = false },
                title = { Text("Backup time") },
                text = {
                    TimePicker(state = timePickerState)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            storage.setAutoBackupHour(timePickerState.hour)
                            storage.setAutoBackupMinute(timePickerState.minute)
                            autoBackupHour = timePickerState.hour
                            autoBackupMinute = timePickerState.minute
                            AutoBackupScheduler.reschedule(context.applicationContext)
                            showAutoBackupTimePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAutoBackupTimePicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (showImportBackupDialog) {
        AlertDialog(
            onDismissRequest = { showImportBackupDialog = false },
            title = { Text("Restore full backup") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(importBackupDialogScrollState)
                ) {
                    Text(
                        "This replaces all products, customers, and invoice history on this device. " +
                            "Existing invoice PDFs stored in the app will be removed and replaced from the backup. " +
                            "This cannot be undone.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    if (scannedBackupFiles.isNotEmpty()) {
                        Text("Latest backup on this device", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        scannedBackupFiles.forEach { file ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showImportBackupDialog = false
                                        pendingRestoreFile = file
                                        pendingRestoreUri = null
                                        showRestoreConfirmDialog = true
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    ) {
                                        Text(
                                            "Tap to restore",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            file.name,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            backupScanDateFormat.format(Date(file.lastModified())),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Text(
                            "No backup .zip files were found in this app’s storage (exports, automatic backup, or app Downloads). " +
                                "Use the button below to pick a file from Google Drive, Downloads, or anywhere else.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            restoreBackupLauncher.launch(
                                arrayOf("application/zip", "application/octet-stream", "*/*")
                            )
                        }
                    ) {
                        Text("Choose backup file…")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImportBackupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreUri = null
                pendingRestoreFile = null
            },
            title = { Text("Restore backup?") },
            text = {
                Text(
                    "All data on this device (products, customers, invoices, and stored invoice PDFs) will be replaced " +
                        "with the contents of this backup file. This cannot be undone.\n\nDo you still want to continue?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = pendingRestoreUri
                        val file = pendingRestoreFile
                        importScope.launch {
                            val validationErr = withContext(Dispatchers.IO) {
                                when {
                                    uri != null -> validateFullBackupZip(context.applicationContext, uri)
                                    file != null -> validateFullBackupZipFile(file)
                                    else -> "No backup selected."
                                }
                            }
                            if (validationErr != null) {
                                Toast.makeText(context, validationErr, Toast.LENGTH_LONG).show()
                                showRestoreConfirmDialog = false
                                pendingRestoreUri = null
                                pendingRestoreFile = null
                                return@launch
                            }
                            when {
                                uri != null -> onImportFullBackup(uri)
                                file != null -> onImportFullBackupFile(file)
                            }
                            showRestoreConfirmDialog = false
                            pendingRestoreUri = null
                            pendingRestoreFile = null
                        }
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmDialog = false
                        pendingRestoreUri = null
                        pendingRestoreFile = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportScreen(
    records: List<InvoiceRecord>,
    customers: List<Customer>,
    onReportExported: (File) -> Unit
) {
    val context = LocalContext.current
    val reportTypeOptions = listOf("Month-wise", "Customer-wise")
    val formatOptions = listOf("PDF", "Excel")
    val monthOptions = remember(records) {
        buildList {
            add("All Months")
            addAll(records.map { monthKey(it.createdAtMillis) }.distinct().sortedDescending())
        }
    }
    val customerOptions = remember(records) {
        buildList {
            add("All Customers")
            addAll(records.map { it.customerName }.distinct().sorted())
        }
    }

    var reportType by remember { mutableStateOf(reportTypeOptions.first()) }
    var exportFormat by remember { mutableStateOf(formatOptions.first()) }
    var monthFilter by remember { mutableStateOf("All Months") }
    var customerFilter by remember { mutableStateOf("All Customers") }
    var reportTypeExpanded by remember { mutableStateOf(false) }
    var formatExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var customerExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(monthOptions, monthFilter) {
        if (monthFilter !in monthOptions) monthFilter = "All Months"
    }
    LaunchedEffect(customerOptions, customerFilter) {
        if (customerFilter !in customerOptions) customerFilter = "All Customers"
    }

    val filtered = remember(records, monthFilter, customerFilter) {
        records.filter {
            val monthMatch = monthFilter == "All Months" || monthKey(it.createdAtMillis) == monthFilter
            val customerMatch = customerFilter == "All Customers" || it.customerName == customerFilter
            monthMatch && customerMatch
        }
    }
    val rows = remember(filtered, reportType) { buildReportRows(filtered, reportType) }
    val previewRows = remember(records) {
        val now = Instant.now().atZone(ZoneId.systemDefault())
        val currentMonth = now.year * 12 + now.monthValue
        val last12 = records.filter { record ->
            if (record.cancelled || record.createdAtMillis <= 0L) return@filter false
            val d = Instant.ofEpochMilli(record.createdAtMillis).atZone(ZoneId.systemDefault())
            val recordMonth = d.year * 12 + d.monthValue
            currentMonth - recordMonth in 0..11
        }
        buildReportRows(last12, "Month-wise")
    }

    val pageScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(pageScrollState)
            .padding(bottom = 24.dp)
    ) {
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
    Text("Preview (Month-wise, Last 12 Months)", fontWeight = FontWeight.SemiBold)
    if (previewRows.isEmpty()) {
        Text("No data for the last 12 months.")
    } else {
        // Column (not LazyColumn) so this screen can use one outer verticalScroll without nested scroll issues.
        Column(modifier = Modifier.fillMaxWidth()) {
            previewRows.take(12).forEach { row ->
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
                createSalesReportPdf(context, reportType, monthFilter, customerFilter, rows, filtered)
            } else {
                createSalesReportExcel(context, reportType, monthFilter, customerFilter, rows, filtered, customers)
            }
            if (file != null) {
                onReportExported(file)
            } else {
                Toast.makeText(context, "Failed to export report", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        Text("Generate & Export")
    }
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
    val activeOnly = records.filter { !it.cancelled }
    val grouped = if (reportType == "Month-wise") {
        activeOnly.groupBy { monthKey(it.createdAtMillis) }
    } else {
        activeOnly.groupBy { it.customerName.ifBlank { "Walk-in Customer" } }
    }
    return grouped.entries
        .map { (key, list) ->
            ReportRow(key = key, invoiceCount = list.size, totalSales = list.sumOf { it.total })
        }
        .sortedByDescending { it.totalSales }
}

fun buildReportDetailRows(records: List<InvoiceRecord>, customers: List<Customer>): List<ReportDetailRow> {
    val customerByPhone = customers
        .mapNotNull { c ->
            val normalized = normalizeWhatsAppPhone(c.phone) ?: return@mapNotNull null
            normalized to c.name
        }
        .toMap()
    return records
        .sortedByDescending { it.createdAtMillis }
        .flatMap { record ->
            val status = if (record.paymentReceived) "Received" else "Pending"
            val invStatus = invoiceRecordStatusLabel(record.cancelled)
            val invType = normalizeInvoiceType(record.invoiceType)
            val shipping = record.shippingCharges
            val customer = record.customerName.ifBlank { "Walk-in Customer" }
            val phone = record.customerPhone
            val mappedName = normalizeWhatsAppPhone(phone)?.let { customerByPhone[it] }.orEmpty()
            if (record.items.isEmpty()) {
                listOf(
                    ReportDetailRow(
                        invoiceNumber = record.invoiceNumber,
                        invoiceType = invType,
                        invoiceDate = record.invoiceDate,
                        customerName = customer,
                        mappedCustomerName = mappedName,
                        customerPhone = phone,
                        itemName = "-",
                        quantity = 0,
                        unitPrice = 0.0,
                        lineTotal = 0.0,
                        shippingCharges = shipping,
                        invoiceTotal = record.total,
                        paymentStatus = status,
                        invoiceStatus = invStatus
                    )
                )
            } else {
                record.items.map { item ->
                    ReportDetailRow(
                        invoiceNumber = record.invoiceNumber,
                        invoiceType = invType,
                        invoiceDate = record.invoiceDate,
                        customerName = customer,
                        mappedCustomerName = mappedName,
                        customerPhone = phone,
                        itemName = item.productName,
                        quantity = item.normalizedQuantity(),
                        unitPrice = item.unitPrice,
                        lineTotal = item.lineTotal(),
                        shippingCharges = shipping,
                        invoiceTotal = record.total,
                        paymentStatus = status,
                        invoiceStatus = invStatus
                    )
                }
            }
        }
}

fun buildInvoicePdfRows(records: List<InvoiceRecord>): List<ReportInvoicePdfRow> {
    return records
        .sortedByDescending { it.createdAtMillis }
        .map { record ->
            ReportInvoicePdfRow(
                invoiceNumber = record.invoiceNumber,
                invoiceType = normalizeInvoiceType(record.invoiceType),
                invoiceStatus = invoiceRecordStatusLabel(record.cancelled),
                customerName = record.customerName.ifBlank { "Walk-in Customer" },
                invoiceDate = record.invoiceDate,
                totalItemQty = record.items.sumOf { it.normalizedQuantity() },
                shippingCharges = record.shippingCharges,
                invoiceTotal = record.total
            )
        }
}

private data class ItemSalesB2bB2cAgg(
    var b2bQty: Int = 0,
    var b2bSales: Double = 0.0,
    var b2cQty: Int = 0,
    var b2cSales: Double = 0.0
) {
    val totalQty: Int get() = b2bQty + b2cQty
    val totalSales: Double get() = b2bSales + b2cSales
}

/** One row per distinct item name with quantity and sales split by B2B vs B2C invoices. */
private fun buildItemSalesB2bB2cRows(records: List<InvoiceRecord>): List<Pair<String, ItemSalesB2bB2cAgg>> {
    val map = linkedMapOf<String, ItemSalesB2bB2cAgg>()
    records.filter { !it.cancelled }.forEach { record ->
        val isB2b = normalizeInvoiceType(record.invoiceType) == "B2B"
        record.items.forEach itemLoop@{ item ->
            val name = item.productName.trim()
            if (name.isEmpty()) return@itemLoop
            val agg = map.getOrPut(name) { ItemSalesB2bB2cAgg() }
            if (isB2b) {
                agg.b2bQty += item.normalizedQuantity()
                agg.b2bSales += item.lineTotal()
            } else {
                agg.b2cQty += item.normalizedQuantity()
                agg.b2cSales += item.lineTotal()
            }
        }
    }
    return map.map { (name, agg) -> name to agg }
        .sortedBy { it.first.lowercase(Locale.getDefault()) }
}

data class CustomerSalesExcelRow(
    val customerName: String,
    val customerPhone: String,
    val invoiceCount: Int,
    val totalSales: Double
)

/** Customer + phone bucket: invoice count and sum of totals for filtered records. */
fun buildCustomerSalesExcelRows(records: List<InvoiceRecord>): List<CustomerSalesExcelRow> {
    return records
        .filter { !it.cancelled }
        .groupBy {
            val n = it.customerName.ifBlank { "Walk-in Customer" }
            val p = it.customerPhone.trim()
            n to p
        }
        .map { (key, list) ->
            CustomerSalesExcelRow(
                customerName = key.first,
                customerPhone = key.second,
                invoiceCount = list.size,
                totalSales = list.sumOf { it.total }
            )
        }
        .sortedWith(
            compareBy<CustomerSalesExcelRow> { it.customerName.lowercase(Locale.getDefault()) }
                .thenBy { it.customerPhone }
        )
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

/** How far back invoice rows are included in the Invoice History tab (lazy-expanded). */
private sealed interface InvoiceHistoryLoadDepth {
    data class LastDays(val days: Int) : InvoiceHistoryLoadDepth
    data object AllTime : InvoiceHistoryLoadDepth
}

private fun requiredLoadDepthForDateFilter(filter: String): InvoiceHistoryLoadDepth = when (filter) {
    "Last 7 Days" -> InvoiceHistoryLoadDepth.LastDays(7)
    "Last 30 Days" -> InvoiceHistoryLoadDepth.LastDays(30)
    "Last 90 Days" -> InvoiceHistoryLoadDepth.LastDays(90)
    "All Time" -> InvoiceHistoryLoadDepth.AllTime
    else -> InvoiceHistoryLoadDepth.LastDays(7)
}

private fun loadDepthCovers(loaded: InvoiceHistoryLoadDepth, required: InvoiceHistoryLoadDepth): Boolean {
    if (loaded is InvoiceHistoryLoadDepth.AllTime) return true
    if (required is InvoiceHistoryLoadDepth.AllTime) return false
    val l = loaded as InvoiceHistoryLoadDepth.LastDays
    val r = required as InvoiceHistoryLoadDepth.LastDays
    return l.days >= r.days
}

private fun mergeLoadDepth(
    current: InvoiceHistoryLoadDepth,
    required: InvoiceHistoryLoadDepth
): InvoiceHistoryLoadDepth {
    if (required is InvoiceHistoryLoadDepth.AllTime) return InvoiceHistoryLoadDepth.AllTime
    if (current is InvoiceHistoryLoadDepth.AllTime) return InvoiceHistoryLoadDepth.AllTime
    val r = (required as InvoiceHistoryLoadDepth.LastDays).days
    val c = (current as InvoiceHistoryLoadDepth.LastDays).days
    return InvoiceHistoryLoadDepth.LastDays(maxOf(c, r))
}

/** UI / reports: human-readable invoice lifecycle state. */
fun invoiceRecordStatusLabel(cancelled: Boolean): String = if (cancelled) "Cancelled" else "Active"

fun money(value: Double): String {
    return value.roundToInt().toString()
}

private fun MutableList<Product>.sortProductsByNameAsc() {
    val sorted = sortedBy { it.name.trim().lowercase(Locale.getDefault()) }
    clear()
    addAll(sorted)
}

private fun MutableList<Customer>.sortCustomersByNameAsc() {
    val sorted = sortedBy { it.name.trim().lowercase(Locale.getDefault()) }
    clear()
    addAll(sorted)
}

/** Normalizes persisted / UI invoice type to B2C or B2B. */
fun normalizeInvoiceType(raw: String): String =
    if (raw.trim().uppercase(Locale.getDefault()) == "B2B") "B2B" else "B2C"

/** POI [Sheet.autoSizeColumn] often crashes on Android; use safe widths as fallback. */
private fun Sheet.safeAutoSizeColumnsInclusive(maxColumnIndex: Int) {
    for (i in 0..maxColumnIndex) {
        try {
            autoSizeColumn(i)
            val w = getColumnWidth(i)
            if (w > 18000) setColumnWidth(i, 18000)
        } catch (_: Throwable) {
            try {
                setColumnWidth(i, 18 * 256)
            } catch (_: Throwable) {
                // ignore
            }
        }
    }
}

private fun formatAutoBackupTimeLabel(context: Context, hour: Int, minute: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
        set(Calendar.MINUTE, minute.coerceIn(0, 59))
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return DateFormat.getTimeFormat(context).format(cal.time)
}

/**
 * JSON + SharedPreferences persistence. Keep `prefs` name and JSON field names stable so upgrades
 * over older installs keep products, customers, invoice history, counters, and auto-backup settings.
 */
class BillingStorage(context: Context) {
    private val prefs = context.getSharedPreferences("billing_store", Context.MODE_PRIVATE)
    private val productsKey = "products"
    private val customersKey = "customers"
    private val invoiceSequenceKey = "invoice_sequence"
    private val invoiceHistoryKey = "invoice_history"
    private val autoBackupEnabledKey = "auto_backup_enabled"
    private val autoBackupIntervalDaysKey = "auto_backup_interval_days"
    private val autoBackupHourKey = "auto_backup_hour"
    private val autoBackupMinuteKey = "auto_backup_minute"

    fun isAutoBackupEnabled(): Boolean = prefs.getBoolean(autoBackupEnabledKey, false)

    fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(autoBackupEnabledKey, enabled).apply()
    }

    /** 1 = daily at midnight, 7 = weekly, etc. */
    fun getAutoBackupIntervalDays(): Int = prefs.getInt(autoBackupIntervalDaysKey, 1).coerceIn(1, 30)

    fun setAutoBackupIntervalDays(days: Int) {
        prefs.edit().putInt(autoBackupIntervalDaysKey, days.coerceIn(1, 30)).apply()
    }

    /** Local time (0–23 / 0–59) when automatic backup should run. Default 0:00 matches previous midnight behavior. */
    fun getAutoBackupHour(): Int = prefs.getInt(autoBackupHourKey, 0).coerceIn(0, 23)

    fun setAutoBackupHour(hour: Int) {
        prefs.edit().putInt(autoBackupHourKey, hour.coerceIn(0, 23)).apply()
    }

    fun getAutoBackupMinute(): Int = prefs.getInt(autoBackupMinuteKey, 0).coerceIn(0, 59)

    fun setAutoBackupMinute(minute: Int) {
        prefs.edit().putInt(autoBackupMinuteKey, minute.coerceIn(0, 59)).apply()
    }

    private val lastAutoBackupSuccessKey = "last_auto_backup_success_millis"

    fun getLastAutoBackupSuccessMillis(): Long = prefs.getLong(lastAutoBackupSuccessKey, 0L)

    fun setLastAutoBackupSuccessMillis(millis: Long) {
        prefs.edit().putLong(lastAutoBackupSuccessKey, millis).apply()
    }

    fun loadProducts(): List<Product> {
        val raw = prefs.getString(productsKey, "[]") ?: "[]"
        val arr = try {
            JSONArray(raw)
        } catch (_: Exception) {
            return emptyList()
        }
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val name = obj.optString("name", "").trim()
                val price = obj.optDouble("defaultPrice", -1.0)
                if (name.isNotBlank() && price >= 0.0) {
                    add(Product(name, price.roundToInt().toDouble()))
                }
            }
        }
    }

    fun saveProducts(items: List<Product>) {
        val arr = JSONArray()
        items.forEach {
            arr.put(
                JSONObject().apply {
                    put("name", it.name)
                    put("defaultPrice", it.defaultPrice.roundToInt())
                }
            )
        }
        prefs.edit().putString(productsKey, arr.toString()).apply()
    }

    fun loadCustomers(): List<Customer> {
        val raw = prefs.getString(customersKey, "[]") ?: "[]"
        val arr = try {
            JSONArray(raw)
        } catch (_: Exception) {
            return emptyList()
        }
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val name = obj.optString("name", "").trim()
                val phone = obj.optString("phone", "").trim()
                if (name.isBlank() || phone.isBlank()) continue
                add(
                    Customer(
                        name,
                        phone,
                        obj.optString("gst", ""),
                        obj.optString("address", "")
                    )
                )
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
                    put("address", it.address)
                }
            )
        }
        prefs.edit().putString(customersKey, arr.toString()).apply()
    }

    fun loadInvoiceHistory(): List<InvoiceRecord> {
        val raw = prefs.getString(invoiceHistoryKey, "[]") ?: "[]"
        val arr = try {
            JSONArray(raw)
        } catch (_: Exception) {
            return emptyList()
        }
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val invoiceNumber = obj.optString("invoiceNumber", "").trim()
                if (invoiceNumber.isEmpty()) continue
                val invoiceDate = obj.optString("invoiceDate", "")
                try {
                    add(
                        InvoiceRecord(
                            invoiceNumber = invoiceNumber,
                            invoiceDate = invoiceDate,
                            customerName = obj.optString("customerName", ""),
                            customerPhone = obj.optString("customerPhone", ""),
                            items = parseItems(obj.optJSONArray("items")),
                            shippingCharges = obj.optDouble("shippingCharges", 0.0),
                            total = obj.optDouble("total", 0.0),
                            filePath = obj.optString("filePath", ""),
                            createdAtMillis = obj.optLong(
                                "createdAtMillis",
                                parseDateToMillis(invoiceDate)
                            ),
                            paymentReceived = obj.optBoolean("paymentReceived", false),
                            invoiceType = normalizeInvoiceType(obj.optString("invoiceType", "B2C")),
                            notes = obj.optString("notes", ""),
                            cancelled = obj.optBoolean("cancelled", false)
                        )
                    )
                } catch (_: Exception) {
                    // Skip a single corrupt row; keeps the rest of history loadable after upgrade.
                }
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
                    put("invoiceType", normalizeInvoiceType(it.invoiceType))
                    put("notes", it.notes)
                    put("cancelled", it.cancelled)
                }
            )
        }
        prefs.edit().putString(invoiceHistoryKey, arr.toString()).apply()
    }

    /** Current invoice counter (last consumed index). Used for full backup/restore. */
    fun getInvoiceSequenceValue(): Int = prefs.getInt(invoiceSequenceKey, 0)

    fun setInvoiceSequenceValue(value: Int) {
        prefs.edit().putInt(invoiceSequenceKey, value.coerceAtLeast(0)).apply()
    }

    /** Replace all persisted app data (products, customers, invoice history, invoice counter). */
    fun applyFullRestore(
        products: List<Product>,
        customers: List<Customer>,
        invoiceHistory: List<InvoiceRecord>,
        invoiceSequence: Int
    ) {
        saveProducts(products)
        saveCustomers(customers)
        saveInvoiceHistory(invoiceHistory)
        setInvoiceSequenceValue(invoiceSequence)
    }

    fun exportProductsJson(): String {
        return prefs.getString(productsKey, "[]") ?: "[]"
    }

    fun exportCustomersJson(): String {
        return prefs.getString(customersKey, "[]") ?: "[]"
    }

    fun importProductsJson(json: String): List<Product> {
        val arr = JSONArray(json)
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val name = obj.optString("name", "").trim()
                val defaultPrice = obj.optDouble("defaultPrice", -1.0)
                if (name.isNotBlank() && defaultPrice >= 0.0) {
                    add(Product(name, defaultPrice.roundToInt().toDouble()))
                }
            }
        }.distinctBy { it.name.trim().lowercase(Locale.getDefault()) }
    }

    fun importCustomersJson(json: String): List<Customer> {
        val arr = JSONArray(json)
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val name = obj.optString("name", "").trim()
                val phone = obj.optString("phone", "").trim()
                val gst = obj.optString("gst", "").trim()
                if (name.isNotBlank() && phone.isNotBlank()) {
                    val address = obj.optString("address", "").trim()
                    add(Customer(name, phone, gst, address))
                }
            }
        }.distinctBy { normalizeWhatsAppPhone(it.phone) ?: it.phone }
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
                        quantity = obj.optDouble("quantity", 0.0).roundToInt().coerceAtLeast(0).toDouble(),
                        unitPrice = obj.optDouble("unitPrice", 0.0).roundToInt().coerceAtLeast(0).toDouble()
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
                    put("quantity", it.normalizedQuantity())
                    put("unitPrice", it.unitPrice.roundToInt().coerceAtLeast(0))
                }
            )
        }
        return arr
    }
}

private const val FULL_BACKUP_FORMAT_VERSION = 1
private const val FULL_BACKUP_MANIFEST = "manifest.json"

private fun sanitizeBackupPathComponent(raw: String): String {
    val t = raw.trim().replace(Regex("[^a-zA-Z0-9._-]+"), "_").take(64)
    return if (t.isEmpty()) "inv" else t
}

private fun invoiceLineItemsToJsonArray(items: List<InvoiceLineItem>): JSONArray {
    val arr = JSONArray()
    items.forEach {
        arr.put(
            JSONObject().apply {
                put("productName", it.productName)
                put("quantity", it.normalizedQuantity())
                put("unitPrice", it.unitPrice.roundToInt().coerceAtLeast(0))
            }
        )
    }
    return arr
}

private fun parseInvoiceLineItemsFromBackupJson(arr: JSONArray?): List<InvoiceLineItem> {
    if (arr == null) return emptyList()
    return buildList {
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            add(
                InvoiceLineItem(
                    productName = obj.optString("productName", ""),
                    quantity = obj.optDouble("quantity", 0.0).roundToInt().coerceAtLeast(0).toDouble(),
                    unitPrice = obj.optDouble("unitPrice", 0.0).roundToInt().coerceAtLeast(0).toDouble()
                )
            )
        }
    }
}

private fun invoiceRecordToBackupJson(rec: InvoiceRecord, storedPdfPathInZip: String?): JSONObject {
    return JSONObject().apply {
        put("invoiceNumber", rec.invoiceNumber)
        put("invoiceDate", rec.invoiceDate)
        put("customerName", rec.customerName)
        put("customerPhone", rec.customerPhone)
        put("items", invoiceLineItemsToJsonArray(rec.items))
        put("shippingCharges", rec.shippingCharges)
        put("total", rec.total)
        put("createdAtMillis", rec.createdAtMillis)
        put("paymentReceived", rec.paymentReceived)
        put("invoiceType", normalizeInvoiceType(rec.invoiceType))
        put("notes", rec.notes)
        put("cancelled", rec.cancelled)
        if (storedPdfPathInZip != null) put("storedPdf", storedPdfPathInZip)
    }
}

/**
 * Writes full backup bytes into [zos] (manifest + pdfs/).
 */
private fun writeFullBackupZipEntries(storage: BillingStorage, zos: ZipOutputStream) {
    val invoices = storage.loadInvoiceHistory()
    val invArr = JSONArray()
    invoices.forEachIndexed { idx, rec ->
        val entryName = "pdfs/${idx}_${sanitizeBackupPathComponent(rec.invoiceNumber)}.pdf"
        val pdf = File(rec.filePath)
        val stored: String? = if (pdf.isFile && pdf.exists()) {
            zos.putNextEntry(ZipEntry(entryName))
            pdf.inputStream().use { input -> input.copyTo(zos) }
            zos.closeEntry()
            entryName
        } else {
            null
        }
        invArr.put(invoiceRecordToBackupJson(rec, stored))
    }
    val manifest = JSONObject().apply {
        put("formatVersion", FULL_BACKUP_FORMAT_VERSION)
        put("exportedAtMillis", System.currentTimeMillis())
        put("invoiceSequence", storage.getInvoiceSequenceValue())
        put("products", JSONArray(storage.exportProductsJson()))
        put("customers", JSONArray(storage.exportCustomersJson()))
        put("invoices", invArr)
    }
    zos.putNextEntry(ZipEntry(FULL_BACKUP_MANIFEST))
    zos.write(manifest.toString().toByteArray(Charsets.UTF_8))
    zos.closeEntry()
}

/** Single manual export filename under [CrumbsAndSoulBackups] — avoids accumulating timestamped copies. */
private const val MANUAL_FULL_BACKUP_ZIP_NAME = "CrumbsAndSoul_full_backup.zip"

private val LEGACY_TIMESTAMPED_MANUAL_BACKUP_NAME =
    Regex("^CrumbsAndSoul_full_backup_\\d+\\.zip$", RegexOption.IGNORE_CASE)

/**
 * Migrates old `CrumbsAndSoul_full_backup_<millis>.zip` files to a single [MANUAL_FULL_BACKUP_ZIP_NAME]
 * (newest wins) and deletes extra timestamped copies so only one manual backup remains.
 */
private fun cleanupLegacyManualBackupZips(backupDir: File) {
    try {
        if (!backupDir.isDirectory) return
        val legacy = backupDir.listFiles()
            ?.filter { it.isFile && it.name.matches(LEGACY_TIMESTAMPED_MANUAL_BACKUP_NAME) }
            ?: return
        if (legacy.isEmpty()) return
        val target = File(backupDir, MANUAL_FULL_BACKUP_ZIP_NAME)
        if (!target.exists()) {
            val newest = legacy.maxByOrNull { it.lastModified() } ?: return
            newest.renameTo(target)
        }
        backupDir.listFiles()
            ?.filter { it.isFile && it.name.matches(LEGACY_TIMESTAMPED_MANUAL_BACKUP_NAME) }
            ?.forEach { it.delete() }
    } catch (_: Exception) {
    }
}

/**
 * Writes a single full backup .zip at [zipFile], replacing any existing file.
 * @return true if the file was written successfully.
 */
@Suppress("UNUSED_PARAMETER")
fun buildFullBackupZipToFile(context: Context, storage: BillingStorage, zipFile: File): Boolean {
    return try {
        zipFile.parentFile?.mkdirs()
        if (zipFile.exists()) zipFile.delete()
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            writeFullBackupZipEntries(storage, zos)
        }
        zipFile.isFile && zipFile.length() > 0L
    } catch (_: Exception) {
        false
    }
}

/**
 * Writes a .zip with [FULL_BACKUP_MANIFEST] plus optional PDFs under `pdfs/`.
 * Share the file to cloud/USB/Drive for transfer to a new device.
 */
fun buildFullBackupZip(context: Context, storage: BillingStorage): File? {
    return try {
        val backupDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "CrumbsAndSoulBackups"
        ).apply { mkdirs() }
        cleanupLegacyManualBackupZips(backupDir)
        val zipFile = File(backupDir, MANUAL_FULL_BACKUP_ZIP_NAME)
        if (buildFullBackupZipToFile(context, storage, zipFile)) zipFile else null
    } catch (_: Exception) {
        null
    }
}

/**
 * Restores app data from a backup zip created by [buildFullBackupZip].
 * @return `null` on success, or an error message.
 */
fun restoreFullBackupFromZip(context: Context, storage: BillingStorage, uri: Uri): String? {
    val stream = context.contentResolver.openInputStream(uri) ?: return "Could not read backup file"
    return stream.use { restoreFullBackupFromZipStream(context, storage, it) }
}

/**
 * Restores from a local [.zip] file (e.g. scanned backups under app storage).
 */
fun restoreFullBackupFromZipFile(context: Context, storage: BillingStorage, file: File): String? {
    return try {
        if (!file.isFile || !file.canRead()) return "Could not read backup file"
        FileInputStream(file).use { restoreFullBackupFromZipStream(context, storage, it) }
    } catch (e: Exception) {
        e.message ?: "Restore failed"
    }
}

/**
 * Validates a backup from a content [Uri] without modifying app data.
 * Call this before [restoreFullBackupFromZip] so a bad file does not wipe existing data.
 * @return `null` if the backup looks valid, or an error message.
 */
fun validateFullBackupZip(context: Context, uri: Uri): String? {
    return try {
        val stream = context.contentResolver.openInputStream(uri) ?: return "Could not open backup file."
        stream.use { validateFullBackupZipInputStream(it) }
    } catch (e: Exception) {
        e.message ?: "Could not validate backup file."
    }
}

/**
 * Validates a local backup [.zip] without modifying app data.
 * @return `null` if valid, or an error message.
 */
fun validateFullBackupZipFile(file: File): String? {
    return try {
        if (!file.isFile || !file.canRead()) return "Could not read backup file."
        if (file.length() < 32L) return "Backup file is too small or empty."
        FileInputStream(file).use { validateFullBackupZipInputStream(it) }
    } catch (e: Exception) {
        e.message ?: "Could not validate backup file."
    }
}

/**
 * Reads and checks the archive and [FULL_BACKUP_MANIFEST] structure (no DB changes).
 */
private fun validateFullBackupZipInputStream(rawIn: InputStream): String? {
    return try {
        var manifestJson: String? = null
        BufferedInputStream(rawIn).use { buffered ->
            ZipInputStream(buffered).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (!entry.isDirectory && name == FULL_BACKUP_MANIFEST) {
                        manifestJson = String(zis.readBytes(), Charsets.UTF_8)
                        break
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }
        val manifestStr = manifestJson
            ?: return "This file is not a valid Crumbs & Soul backup (missing manifest.json)."
        val manifest = try {
            JSONObject(manifestStr)
        } catch (_: Exception) {
            return "Backup manifest is not valid JSON. The file may be corrupted."
        }
        val ver = manifest.optInt("formatVersion", 0)
        if (ver != FULL_BACKUP_FORMAT_VERSION) {
            return "Unsupported backup version (found $ver, need $FULL_BACKUP_FORMAT_VERSION)."
        }
        if (manifest.optJSONArray("products") == null) {
            return "Backup manifest is invalid (missing or bad \"products\")."
        }
        if (manifest.optJSONArray("customers") == null) {
            return "Backup manifest is invalid (missing or bad \"customers\")."
        }
        val invArr = manifest.optJSONArray("invoices")
            ?: return "Backup manifest is invalid (missing or bad \"invoices\")."
        for (i in 0 until invArr.length()) {
            val o = invArr.optJSONObject(i)
                ?: return "Backup manifest is invalid (invoice entry $i is not an object)."
            if (!o.has("invoiceNumber")) {
                return "Backup manifest is invalid (invoice $i missing invoice number)."
            }
        }
        null
    } catch (e: ZipException) {
        "The backup file is corrupted or incomplete (archive error)."
    } catch (e: Exception) {
        e.message ?: "Could not validate backup file."
    }
}

/**
 * Scans app-accessible locations for [.zip] backups (newest first).
 * Includes export folder, app Downloads, and the automatic backup file if present.
 * Returns **at most one** file — the newest candidate — so restore UI stays simple and storage stays minimal.
 */
fun scanBackupZipFiles(context: Context): List<File> {
    val backupDir = try {
        File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "CrumbsAndSoulBackups"
        )
    } catch (_: Exception) {
        null
    }
    backupDir?.let { cleanupLegacyManualBackupZips(it) }

    val byPath = LinkedHashMap<String, File>()
    fun add(f: File?) {
        if (f == null || !f.isFile) return
        if (!f.name.endsWith(".zip", ignoreCase = true)) return
        if (f.length() < 32L) return
        byPath[f.absolutePath] = f
    }
    try {
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.let { base ->
            File(base, "CrumbsAndSoulBackups").listFiles()?.forEach { add(it) }
        }
    } catch (_: Exception) {
    }
    try {
        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.listFiles()?.forEach { add(it) }
    } catch (_: Exception) {
    }
    try {
        add(AutoBackupScheduler.autoBackupZipFile(context))
    } catch (_: Exception) {
    }
    val newest = byPath.values.sortedByDescending { it.lastModified() }
    return newest.take(1)
}

private fun applyFullRestoreToUi(
    context: Context,
    storage: BillingStorage,
    products: MutableList<Product>,
    customers: MutableList<Customer>,
    invoiceHistory: MutableList<InvoiceRecord>,
    invoiceDraft: InvoiceDraftState
) {
    products.clear()
    products.addAll(
        storage.loadProducts()
            .sortedBy { it.name.trim().lowercase(Locale.getDefault()) }
    )
    customers.clear()
    customers.addAll(
        storage.loadCustomers()
            .sortedBy { it.name.trim().lowercase(Locale.getDefault()) }
    )
    invoiceHistory.clear()
    invoiceHistory.addAll(storage.loadInvoiceHistory())
    invoiceDraft.selectedCustomerName = ""
    invoiceDraft.selectedCustomerPhone = ""
    invoiceDraft.customerSearch = ""
    invoiceDraft.productSearch = ""
    invoiceDraft.quantityInput = "1"
    invoiceDraft.priceInput = ""
    invoiceDraft.invoiceDate = ""
    invoiceDraft.editingInvoiceNumber = null
    invoiceDraft.shippingChargesInput = ""
    invoiceDraft.notes = ""
    invoiceDraft.invoiceType = "B2C"
    invoiceDraft.lineItems.clear()
    invoiceDraft.invoiceNumber = storage.previewInvoiceNumber()
    Toast.makeText(
        context,
        "Backup restored. All data was replaced.",
        Toast.LENGTH_LONG
    ).show()
}

private fun restoreFullBackupFromZipStream(context: Context, storage: BillingStorage, rawIn: InputStream): String? {
    return try {
        val pdfBytesByPath = mutableMapOf<String, ByteArray>()
        var manifestJson: String? = null
        BufferedInputStream(rawIn).use { buffered ->
            ZipInputStream(buffered).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (!entry.isDirectory) {
                        when {
                            name == FULL_BACKUP_MANIFEST -> {
                                manifestJson = String(zis.readBytes(), Charsets.UTF_8)
                            }
                            name.startsWith("pdfs/") -> {
                                pdfBytesByPath[name] = zis.readBytes()
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }

        val manifestStr = manifestJson ?: return "Backup is missing manifest"
        val manifest = JSONObject(manifestStr)
        val ver = manifest.optInt("formatVersion", 0)
        if (ver != FULL_BACKUP_FORMAT_VERSION) {
            return "Unsupported backup version (found $ver, need $FULL_BACKUP_FORMAT_VERSION)"
        }

        val products = storage.importProductsJson(manifest.getJSONArray("products").toString())
        val customers = storage.importCustomersJson(manifest.getJSONArray("customers").toString())
        val invArr = manifest.getJSONArray("invoices")
        val seq = manifest.optInt("invoiceSequence", 0).coerceAtLeast(0)

        val invoicesFolder = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "CrumbsAndSoulInvoices"
        ).apply { mkdirs() }
        invoicesFolder.listFiles()
            ?.filter { it.isFile && it.extension.equals("pdf", ignoreCase = true) }
            ?.forEach { runCatching { it.delete() } }

        val restored = mutableListOf<InvoiceRecord>()
        for (i in 0 until invArr.length()) {
            val o = invArr.getJSONObject(i)
            val invoiceNumber = o.getString("invoiceNumber")
            val cancelledRestore = o.optBoolean("cancelled", false)
            val storedPdf = o.optString("storedPdf", "")
            val bytes = if (storedPdf.isNotBlank()) pdfBytesByPath[storedPdf] else null
            var filePath = ""
            if (bytes != null && bytes.isNotEmpty()) {
                val dest = File(invoicesFolder, "Invoice_${invoiceNumber}_restore_$i.pdf")
                dest.outputStream().use { it.write(bytes) }
                filePath = dest.absolutePath
            }
            if (filePath.isBlank()) {
                val data = InvoiceData(
                    invoiceNumber = invoiceNumber,
                    invoiceDate = o.getString("invoiceDate"),
                    customerName = o.getString("customerName"),
                    customerPhone = o.optString("customerPhone", ""),
                    items = parseInvoiceLineItemsFromBackupJson(o.optJSONArray("items")),
                    shippingCharges = o.optDouble("shippingCharges", 0.0),
                    total = o.getDouble("total"),
                    invoiceType = normalizeInvoiceType(o.optString("invoiceType", "B2C")),
                    notes = o.optString("notes", "")
                )
                val regenerated = InvoicePdfGenerator.createInvoicePdf(context, data, cancelledRestore)
                filePath = regenerated?.absolutePath.orEmpty()
            }
            restored.add(
                InvoiceRecord(
                    invoiceNumber = invoiceNumber,
                    invoiceDate = o.getString("invoiceDate"),
                    customerName = o.getString("customerName"),
                    customerPhone = o.optString("customerPhone", ""),
                    items = parseInvoiceLineItemsFromBackupJson(o.optJSONArray("items")),
                    shippingCharges = o.optDouble("shippingCharges", 0.0),
                    total = o.getDouble("total"),
                    filePath = filePath,
                    createdAtMillis = o.optLong("createdAtMillis", 0L),
                    paymentReceived = o.optBoolean("paymentReceived", false),
                    invoiceType = normalizeInvoiceType(o.optString("invoiceType", "B2C")),
                    notes = o.optString("notes", ""),
                    cancelled = cancelledRestore
                )
            )
        }

        storage.applyFullRestore(products, customers, restored, seq)
        null
    } catch (e: Exception) {
        e.message ?: "Restore failed"
    }
}

/** Word-wrap for PDF drawing; keeps glyphs within [maxWidth] for the given [paint]. Respects explicit newlines. */
private fun pdfWrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return listOf("")
    val lines = mutableListOf<String>()
    val w = maxWidth.coerceAtLeast(24f)
    for (paragraph in trimmed.split("\n")) {
        var remaining = paragraph.trim()
        if (remaining.isEmpty()) {
            lines.add("")
            continue
        }
        while (remaining.isNotEmpty()) {
            var count = paint.breakText(remaining, true, w, null)
            if (count <= 0) count = 1
            val chunk = remaining.substring(0, count).trimEnd()
            lines.add(if (chunk.isEmpty()) remaining.substring(0, 1) else chunk)
            remaining = remaining.substring(count).trimStart()
        }
    }
    return lines
}

private fun paintLineHeight(paint: Paint): Float {
    val fm = paint.fontMetrics
    return (fm.descent - fm.ascent) + 6f
}

/** Visual height of a left-aligned multi-line block (baselines spaced by [lineHeight]). */
private fun pdfTextBlockHeight(lineCount: Int, lineHeight: Float, fm: Paint.FontMetrics): Float {
    if (lineCount <= 0) return 0f
    if (lineCount == 1) return fm.descent - fm.ascent
    return (lineCount - 1) * lineHeight + (fm.descent - fm.ascent)
}

/** First-line baseline to vertically center a wrapped label block between top and bottom. */
private fun pdfCenteredLabelFirstBaseline(
    top: Float,
    bottom: Float,
    lineCount: Int,
    lineHeight: Float,
    fm: Paint.FontMetrics
): Float {
    val blockH = pdfTextBlockHeight(lineCount, lineHeight, fm)
    val firstLineTop = top + ((bottom - top) - blockH) / 2f
    return firstLineTop - fm.ascent
}

/** Baseline for a single line of text vertically centered between top and bottom. */
private fun pdfCenteredSingleLineBaseline(top: Float, bottom: Float, fm: Paint.FontMetrics): Float {
    val cy = (top + bottom) / 2f
    return cy - (fm.ascent + fm.descent) / 2f
}

/** [tightTrailing]: false = next-line baseline for stacking text; true = snug below last line (e.g. before a rule). */
private fun drawPdfWrappedLeft(
    canvas: Canvas,
    text: String,
    paint: Paint,
    x: Float,
    startBaseline: Float,
    maxWidth: Float,
    tightTrailing: Boolean = false
): Float {
    val lines = pdfWrapText(text, paint, maxWidth)
    if (lines.isEmpty()) return startBaseline
    val lh = paintLineHeight(paint)
    val fm = paint.fontMetrics
    lines.forEachIndexed { i, line ->
        if (line.isNotEmpty()) {
            canvas.drawText(line, x, startBaseline + i * lh, paint)
        }
    }
    val lastBaseline = startBaseline + (lines.size - 1) * lh
    return if (tightTrailing) {
        lastBaseline + fm.descent + 4f
    } else {
        lastBaseline + lh
    }
}

/** Makes near-black pixels transparent so [R.drawable.brand_logo] matches any surface (UI + PDF). */
/** Google review link drafted when sharing from invoice history. */
private const val GOOGLE_REVIEW_SHARE_URL = "https://share.google/OwlE8TZHlEbSKdsWc"

/** WhatsApp Business catalog / menu link shared from customer list. */
private const val WHATSAPP_MENU_CATALOG_URL = "https://wa.me/c/919019508365"

/** UPI ID printed on invoices and encoded in the payment QR. */
private const val INVOICE_UPI_VPA = "hanjanalakshmi@okaxis"
private const val INVOICE_UPI_PAYEE_NAME = "Crumbs & Soul"

/** Matches invoice PDF body fill — QR “light” modules blend with the page. */
private val invoiceQrLightBackgroundColor: Int = Color.parseColor("#F7F4EA")

/** UPI intent string for QR (BHIM / GPay / PhonePe / Paytm). */
private fun buildUpiDeepLink(vpa: String, payeeName: String, amountRupees: Double): String {
    return Uri.parse("upi://pay").buildUpon()
        .appendQueryParameter("pa", vpa)
        .appendQueryParameter("pn", payeeName)
        .appendQueryParameter("am", String.format(Locale.US, "%.2f", amountRupees))
        .appendQueryParameter("cu", "INR")
        .build()
        .toString()
}

private fun createQrCodeBitmap(
    data: String,
    sizePx: Int,
    lightColor: Int = Color.WHITE,
    darkColor: Int = Color.BLACK
): Bitmap? {
    return try {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val matrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val w = matrix.width
        val h = matrix.height
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        for (x in 0 until w) {
            for (y in 0 until h) {
                bmp.setPixel(x, y, if (matrix.get(x, y)) darkColor else lightColor)
            }
        }
        bmp
    } catch (_: Exception) {
        null
    }
}

private fun removeBlackBackgroundFromLogo(source: Bitmap): Bitmap {
    val out = source.copy(Bitmap.Config.ARGB_8888, true)
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

/** Writes a new invoice PDF (optional CANCELLED watermark) and moves it to a unique path, deleting the previous file. */
private fun replaceInvoicePdfForRecord(context: Context, record: InvoiceRecord, stampCancelled: Boolean): File? {
    val data = invoiceDataFromRecord(record)
    val newPdf = InvoicePdfGenerator.createInvoicePdf(context, data, stampCancelled) ?: return null
    val oldPdf = File(record.filePath)
    val dest = File(
        newPdf.parentFile ?: oldPdf.parentFile ?: context.filesDir,
        "Invoice_${record.invoiceNumber}_${System.currentTimeMillis()}.pdf"
    )
    val finalFile = try {
        if (newPdf.absolutePath != dest.absolutePath) newPdf.copyTo(dest, overwrite = true) else newPdf
    } catch (_: Exception) {
        newPdf
    }
    if (newPdf.absolutePath != finalFile.absolutePath && newPdf.exists()) newPdf.delete()
    if (oldPdf.exists() && oldPdf.absolutePath != finalFile.absolutePath) oldPdf.delete()
    return finalFile
}

object InvoicePdfGenerator {
    private fun wrapProductNameLines(text: String, paint: Paint, maxWidth: Float): List<String> =
        pdfWrapText(text, paint, maxWidth)

    private fun drawCancelledInvoiceWatermark(canvas: Canvas) {
        val pageW = 1240f
        val pageH = 1754f
        val cx = pageW / 2f
        val cy = pageH / 2f
        val label = "CANCELLED"
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 190, 50, 50)
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        var textSize = 260f
        paint.textSize = textSize
        val maxTextWidth = pageW * 0.82f
        while (paint.measureText(label) > maxTextWidth && textSize > 96f) {
            textSize -= 12f
            paint.textSize = textSize
        }
        val fm = paint.fontMetrics
        val baselineY = cy - (fm.ascent + fm.descent) / 2f
        canvas.save()
        canvas.rotate(-34f, cx, cy)
        canvas.drawText(label, cx, baselineY, paint)
        canvas.restore()
    }

    fun createInvoicePdf(context: Context, invoice: InvoiceData, stampCancelled: Boolean = false): File? {
        return try {
            val document = PdfDocument()
            var pageNumber = 1
            var page = document.startPage(PdfDocument.PageInfo.Builder(1240, 1754, pageNumber).create())
            var canvas = page.canvas

            fun finishCurrentPage() {
                if (stampCancelled) drawCancelledInvoiceWatermark(canvas)
                document.finishPage(page)
            }

            // Colors aligned with CrumbsAndSoulTheme / BrandLightColors (light).
            val themeBackground = Color.parseColor("#F7F4EA")
            val themeSurface = Color.parseColor("#FFFCF4")
            // Body panel: slightly darker than surface, lighter than header (surfaceVariant).
            val themeBodyPanel = Color.parseColor("#F4EFE3")
            val themeSurfaceVariant = Color.parseColor("#EEE8DA")
            val themeOnSurface = Color.parseColor("#1B1B1B")
            val themeOnSurfaceVariant = Color.parseColor("#4A4A4A")
            val themePrimary = Color.parseColor("#5E6840")
            val themeSecondary = Color.parseColor("#BC9B56")
            val themeTertiary = Color.parseColor("#7A5A1E")
            val themeOutline = Color.parseColor("#B4AE9E")

            // Full-page body fill (no white margins) + dark cream header band on top.
            val bodyPanelFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeBodyPanel
                style = Paint.Style.FILL
            }
            val surfaceVariantFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeSurfaceVariant
                style = Paint.Style.FILL
            }
            val logoBackdropPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeSurface
                style = Paint.Style.FILL
            }
            val mutedLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeOutline
                strokeWidth = 1.2f
            }
            val tableRowLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(255, 212, 206, 190)
                strokeWidth = 1f
            }
            val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themePrimary
                textSize = 46f
                isFakeBoldText = true
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeOnSurface
                textSize = 27f
            }
            val strongPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeOnSurface
                textSize = 30f
                isFakeBoldText = true
            }
            val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themePrimary
                textSize = 54f
                isFakeBoldText = true
            }
            val taglinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeOnSurfaceVariant
                textSize = 22f
            }
            val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeOnSurfaceVariant
                textSize = 22f
            }
            val contactPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeOnSurface
                textSize = 23f
            }
            val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeOnSurfaceVariant
                textSize = 22f
            }
            val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeOnSurface
                textSize = 27f
                isFakeBoldText = true
            }
            val tableHeaderRightPaint = Paint(tableHeaderPaint).apply {
                textAlign = Paint.Align.RIGHT
            }
            val tableHeaderFillPaint = Paint(surfaceVariantFillPaint)
            val bodyRightPaint = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
            val strongRightPaint = Paint(strongPaint).apply { textAlign = Paint.Align.RIGHT }
            val qtyRightPaint = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
            val totalsBoxStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = themeOutline
            }

            // Edge-to-edge page background (PDF page is 1240×1754) — removes white outer border in viewers.
            canvas.drawRect(0f, 0f, 1240f, 1754f, bodyPanelFillPaint)
            // Dark cream header extends below the separator so the rule sits inside the header block.
            val headerSeparatorY = 276f
            val headerCardBottomY = 320f
            canvas.drawRoundRect(RectF(40f, 40f, 1200f, headerCardBottomY), 24f, 24f, surfaceVariantFillPaint)

            val logoBitmapRaw = BitmapFactory.decodeResource(context.resources, R.drawable.brand_logo)
            val logoBitmap = removeBlackBackgroundFromLogo(logoBitmapRaw)
            val logoRect = Rect(70, 78, 190, 198)
            val logoCircle = RectF(70f, 78f, 190f, 198f)
            canvas.drawOval(logoCircle, logoBackdropPaint)
            canvas.save()
            canvas.clipRect(logoRect)
            canvas.drawBitmap(logoBitmap, null, logoRect, null)
            canvas.restore()

            canvas.drawText("Crumbs & Soul", 220f, 138f, brandPaint)
            val taglineStartX = 222f
            canvas.drawText("Rooted Indulgence", taglineStartX, 176f, taglinePaint)

            // Contact row: WhatsApp starts at tagline; symmetric L/R inset; equal gaps between clusters.
            val headerContentLeft = 70f
            val headerContentRight = 1180f
            val contactIconW = 26f
            val contactIconTextGap = 10f
            val waNumber = "+91-9019508365"
            val phoneNumber = "+91-9962355820"
            val instaHandle = "@crumbs_and_soul"
            fun clusterWidth(label: String) =
                contactIconW + contactIconTextGap + contactPaint.measureText(label)
            val wWa = clusterWidth(waNumber)
            val wPhone = clusterWidth(phoneNumber)
            val wInsta = clusterWidth(instaHandle)
            val contactInnerRight = headerContentRight - (taglineStartX - headerContentLeft)
            val rowInnerWidth = contactInnerRight - taglineStartX
            val spaceForGaps = rowInnerWidth - wWa - wPhone - wInsta
            val interClusterGap = (spaceForGaps / 2f).coerceAtLeast(0f)
            val xWa = taglineStartX
            val xPhone = xWa + wWa + interClusterGap
            val xInsta = xPhone + wPhone + interClusterGap
            val instaRightEdge = xInsta + wInsta

            // FSSAI: right-align block (logo + number) with Instagram row.
            val fssaiLogo = BitmapFactory.decodeResource(context.resources, R.drawable.fssai_logo)
            val fssaiLogoW = 90f
            val fssaiLogoTop = 100
            val fssaiLogoBottom = 150
            val fssaiNumber = "21225007001088"
            val fssaiLogoTextGap = 10f
            val fssaiTextW = metaPaint.measureText(fssaiNumber)
            val fssaiBlockW = fssaiLogoW + fssaiLogoTextGap + fssaiTextW
            val fssaiLeft = instaRightEdge - fssaiBlockW
            canvas.drawBitmap(
                fssaiLogo,
                null,
                Rect(
                    fssaiLeft.roundToInt(),
                    fssaiLogoTop,
                    (fssaiLeft + fssaiLogoW).roundToInt(),
                    fssaiLogoBottom
                ),
                null
            )
            canvas.drawText(fssaiNumber, fssaiLeft + fssaiLogoW + fssaiLogoTextGap, 138f, metaPaint)

            val contactBaselineY = 252f
            val contactIconTop = 230
            val contactIconBottom = 256
            val waIcon = BitmapFactory.decodeResource(context.resources, R.drawable.whatsapp_icon)
            val phoneIcon = BitmapFactory.decodeResource(context.resources, R.drawable.phone_icon)
            val instaIcon = BitmapFactory.decodeResource(context.resources, R.drawable.instagram_icon)
            fun drawContactClusterLeft(leftX: Float, bmp: Bitmap, label: String) {
                canvas.drawBitmap(
                    bmp,
                    null,
                    Rect(
                        leftX.roundToInt(),
                        contactIconTop,
                        (leftX + contactIconW).roundToInt(),
                        contactIconBottom
                    ),
                    null
                )
                canvas.drawText(
                    label,
                    leftX + contactIconW + contactIconTextGap,
                    contactBaselineY,
                    contactPaint
                )
            }
            drawContactClusterLeft(xWa, waIcon, waNumber)
            drawContactClusterLeft(xPhone, phoneIcon, phoneNumber)
            drawContactClusterLeft(xInsta, instaIcon, instaHandle)

            canvas.drawLine(60f, headerSeparatorY, 1180f, headerSeparatorY, mutedLinePaint)

            val invTypeLabel = normalizeInvoiceType(invoice.invoiceType)
            val contentLeft = 70f
            val contentRight = 1180f
            // Table geometry first so B2B/B2C + Date share the same right edge as Line Total (with padding).
            val tablePadH = 20f
            val cellPadLeft = 14f
            val cellPadRight = 24f
            val tblLeft = contentLeft + tablePadH
            val tblRight = contentRight - tablePadH
            val amountRightX = tblRight - cellPadRight
            val colItemX = tblLeft + cellPadLeft
            // Fixed numeric column widths (right-aligned) so item text never intrudes; qty/unit/line stay one line each.
            val colLineTotalReserve = 122f
            val colUnitReserve = 132f
            val colQtyReserve = 68f
            val colGap = 14f
            val colUnitRight = amountRightX - colLineTotalReserve - colGap
            val colQtyRight = colUnitRight - colUnitReserve - colGap
            val qtyColumnLeftEdge = colQtyRight - colQtyReserve
            val itemNameMaxWidth = (qtyColumnLeftEdge - colItemX - colGap).coerceAtLeast(100f)

            // Extra space below header card so "INVOICE" clears the header block comfortably.
            val invoiceTitleBaseline = 398f
            canvas.drawText("INVOICE", contentLeft, invoiceTitleBaseline, headingPaint)

            val pillLabel = invTypeLabel
            val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 21f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
                color = if (invTypeLabel == "B2B") {
                    themeTertiary
                } else {
                    themePrimary
                }
            }
            val padHPill = 20f
            val pillH = 32f
            val textW = pillTextPaint.measureText(pillLabel)
            val pillW = textW + padHPill * 2
            val metaRightAlignX = amountRightX
            val pillRight = amountRightX
            val pillLeft = pillRight - pillW
            val fmInvoiceTitle = headingPaint.fontMetrics
            val invTop = invoiceTitleBaseline + fmInvoiceTitle.ascent
            val invBottom = invoiceTitleBaseline + fmInvoiceTitle.descent
            val pillTop = invTop + (invBottom - invTop - pillH) / 2f
            val pillFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = if (invTypeLabel == "B2B") {
                    themeSurface
                } else {
                    themeBackground
                }
            }
            val pillStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = if (invTypeLabel == "B2B") {
                    themeSecondary
                } else {
                    themeOutline
                }
            }
            val pillRect = RectF(pillLeft, pillTop, pillRight, pillTop + pillH)
            canvas.drawRoundRect(pillRect, 8f, 8f, pillFill)
            canvas.drawRoundRect(pillRect, 8f, 8f, pillStroke)
            val fmPill = pillTextPaint.fontMetrics
            val pillTextBaseline = pillTop + pillH / 2f - (fmPill.ascent + fmPill.descent) / 2f
            canvas.drawText(pillLabel, pillLeft + padHPill, pillTextBaseline, pillTextPaint)

            // Metadata: invoice line bold like original; customer/phone regular body, smaller; date aligned with type pill.
            val customerLinePaint = Paint(bodyPaint).apply {
                textSize = 26f
                isFakeBoldText = false
            }
            val dateLinePaint = Paint(bodyPaint).apply {
                textSize = 27f
                textAlign = Paint.Align.RIGHT
            }
            var metaY = 456f
            val metaLabelMaxW = (metaRightAlignX - contentLeft - 28f).coerceAtLeast(120f)
            val invNoLines = pdfWrapText("Invoice No: ${invoice.invoiceNumber}", strongPaint, metaLabelMaxW)
            val strongLh = paintLineHeight(strongPaint)
            invNoLines.forEachIndexed { i, line ->
                val bl = metaY + i * strongLh
                canvas.drawText(line, contentLeft, bl, strongPaint)
                if (i == 0) {
                    canvas.drawText("Date: ${invoice.invoiceDate}", metaRightAlignX, bl, dateLinePaint)
                }
            }
            metaY += invNoLines.size * strongLh + 12f
            val custName = invoice.customerName.ifBlank { "Walk-in Customer" }
            val custFullW = (contentRight - contentLeft - 16f).coerceAtLeast(120f)
            metaY = drawPdfWrappedLeft(canvas, "Customer: $custName", customerLinePaint, contentLeft, metaY, custFullW)
            if (invoice.customerPhone.isNotBlank()) {
                metaY += 8f
                metaY = drawPdfWrappedLeft(canvas, "Phone: ${invoice.customerPhone}", customerLinePaint, contentLeft, metaY, custFullW)
            }
            metaY += 28f
            canvas.drawLine(contentLeft, metaY, contentRight, metaY, mutedLinePaint)

            val fmBody = bodyPaint.fontMetrics
            // Extra line spacing so wrapped item names don’t sit on the row divider.
            val itemLineHeight = (fmBody.descent - fmBody.ascent) + 12f
            val rowDividerBelowText = 24f
            val rowSpacingAfterDivider = 26f
            // Keep entire rows on one page (no wrapped item text on one page and qty/price on another).
            val pageContentBottomY = 1710f

            val headerBandTop = metaY + 38f
            val headerBandBottom = headerBandTop + 56f
            val headerTextBaseline = headerBandTop + 36f
            val headerRuleY = headerBandBottom - 3f

            // Space needed for table header band + at least one single-line item row + divider.
            val minOneItemRowBottom =
                headerBandBottom + 42f + itemLineHeight + fmBody.descent + rowDividerBelowText + rowSpacingAfterDivider
            val needTableOnNewPage =
                invoice.items.isNotEmpty() && minOneItemRowBottom > pageContentBottomY

            val unitRightPaint = Paint(bodyRightPaint)

            fun drawContinuationTableHeaderOnly(): Float {
                val contPaint = Paint(headingPaint).apply { textSize = 34f }
                val titleY = 86f
                canvas.drawText("INVOICE (continued) — ${invoice.invoiceNumber}", contentLeft, titleY, contPaint)
                canvas.drawLine(contentLeft, titleY + 32f, contentRight, titleY + 32f, mutedLinePaint)
                val hbTop = titleY + 56f
                val hbBot = hbTop + 56f
                val htb = hbTop + 36f
                val hrY = hbBot - 3f
                canvas.drawRoundRect(
                    RectF(contentLeft, hbTop, contentRight, hbBot),
                    8f,
                    8f,
                    tableHeaderFillPaint
                )
                canvas.drawText("Item", colItemX, htb, tableHeaderPaint)
                canvas.drawText("Qty", colQtyRight, htb, tableHeaderRightPaint)
                canvas.drawText("Unit Price", colUnitRight, htb, tableHeaderRightPaint)
                canvas.drawText("Line Total", amountRightX, htb, tableHeaderRightPaint)
                canvas.drawLine(tblLeft, hrY, tblRight, hrY, mutedLinePaint)
                return hbBot + 42f
            }

            /** Baseline for first item row on a continuation page (after header block). */
            fun continuationFirstItemBaseline(): Float = 86f + 56f + 56f + 42f

            var rowBaseline = if (needTableOnNewPage) {
                finishCurrentPage()
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(1240, 1754, pageNumber).create())
                canvas = page.canvas
                canvas.drawRect(0f, 0f, 1240f, 1754f, bodyPanelFillPaint)
                drawContinuationTableHeaderOnly()
            } else {
                canvas.drawRoundRect(
                    RectF(contentLeft, headerBandTop, contentRight, headerBandBottom),
                    8f,
                    8f,
                    tableHeaderFillPaint
                )
                canvas.drawText("Item", colItemX, headerTextBaseline, tableHeaderPaint)
                canvas.drawText("Qty", colQtyRight, headerTextBaseline, tableHeaderRightPaint)
                canvas.drawText("Unit Price", colUnitRight, headerTextBaseline, tableHeaderRightPaint)
                canvas.drawText("Line Total", amountRightX, headerTextBaseline, tableHeaderRightPaint)
                canvas.drawLine(tblLeft, headerRuleY, tblRight, headerRuleY, mutedLinePaint)
                headerBandBottom + 42f
            }

            fun startContinuationPageWithTableHeader() {
                finishCurrentPage()
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(1240, 1754, pageNumber).create())
                canvas = page.canvas
                canvas.drawRect(0f, 0f, 1240f, 1754f, bodyPanelFillPaint)
                rowBaseline = drawContinuationTableHeaderOnly()
            }

            fun startBlankTotalsPage() {
                finishCurrentPage()
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(1240, 1754, pageNumber).create())
                canvas = page.canvas
                canvas.drawRect(0f, 0f, 1240f, 1754f, bodyPanelFillPaint)
                rowBaseline = 110f
            }

            var itemIdx = 0
            while (itemIdx < invoice.items.size) {
                val item = invoice.items[itemIdx]
                val nameLines = wrapProductNameLines(item.productName, bodyPaint, itemNameMaxWidth)
                val linesForRow = nameLines.size.coerceAtLeast(1)
                val lastLineBaseline = rowBaseline + (linesForRow - 1) * itemLineHeight
                val rowDividerY = lastLineBaseline + fmBody.descent + rowDividerBelowText
                val rowBottomWithSpacing = rowDividerY + rowSpacingAfterDivider
                if (rowBottomWithSpacing > pageContentBottomY) {
                    val rowSpan = rowBottomWithSpacing - rowBaseline
                    val usableOnFreshPage = pageContentBottomY - continuationFirstItemBaseline()
                    if (rowSpan <= usableOnFreshPage) {
                        startContinuationPageWithTableHeader()
                        continue
                    }
                }
                nameLines.forEachIndexed { idx, line ->
                    canvas.drawText(line, colItemX, rowBaseline + idx * itemLineHeight, bodyPaint)
                }
                canvas.drawText(item.normalizedQuantity().toString(), colQtyRight, rowBaseline, qtyRightPaint)
                canvas.drawText("₹${money(item.unitPrice)}", colUnitRight, rowBaseline, unitRightPaint)
                canvas.drawText("₹${money(item.lineTotal())}", amountRightX, rowBaseline, bodyRightPaint)
                canvas.drawLine(tblLeft, rowDividerY, tblRight, rowDividerY, tableRowLinePaint)
                rowBaseline = rowDividerY + rowSpacingAfterDivider
                itemIdx++
            }

            val totalsLeft = tblRight - 464f
            val totalsLabelMaxW = (amountRightX - totalsLeft - 40f).coerceAtLeast(100f)
            if (rowBaseline + 320f > 1680f) {
                startBlankTotalsPage()
            }
            var y = rowBaseline + 24f
            if (invoice.shippingCharges > 0.0) {
                val shipLines = pdfWrapText("Shipping Charges:", bodyPaint, totalsLabelMaxW)
                val shipLh = paintLineHeight(bodyPaint)
                val fmShipLabel = bodyPaint.fontMetrics
                val shipBlockH = pdfTextBlockHeight(shipLines.size, shipLh, fmShipLabel)
                val shipPadV = 16f
                val shipTop = y
                val shipBot = shipTop + shipPadV * 2 + shipBlockH
                canvas.drawRoundRect(RectF(totalsLeft, shipTop, tblRight, shipBot), 8f, 8f, surfaceVariantFillPaint)
                canvas.drawRoundRect(RectF(totalsLeft, shipTop, tblRight, shipBot), 8f, 8f, totalsBoxStrokePaint)
                val shipLabelBaseline = pdfCenteredLabelFirstBaseline(
                    shipTop, shipBot, shipLines.size, shipLh, fmShipLabel
                )
                shipLines.forEachIndexed { i, ln ->
                    canvas.drawText(ln, totalsLeft + 20f, shipLabelBaseline + i * shipLh, bodyPaint)
                }
                val shipAmtBaseline = pdfCenteredSingleLineBaseline(shipTop, shipBot, bodyRightPaint.fontMetrics)
                canvas.drawText("₹${money(invoice.shippingCharges)}", amountRightX, shipAmtBaseline, bodyRightPaint)
                y = shipBot + 32f
            }
            val totalLabelLines = pdfWrapText("Total Amount:", strongPaint, totalsLabelMaxW)
            val totalStrongLh = paintLineHeight(strongPaint)
            val fmTotalLabel = strongPaint.fontMetrics
            val totBlockH = pdfTextBlockHeight(totalLabelLines.size, totalStrongLh, fmTotalLabel)
            val totPadV = 16f
            val totTop = y
            val totBot = totTop + totPadV * 2 + totBlockH
            canvas.drawRoundRect(RectF(totalsLeft, totTop, tblRight, totBot), 8f, 8f, surfaceVariantFillPaint)
            canvas.drawRoundRect(RectF(totalsLeft, totTop, tblRight, totBot), 8f, 8f, totalsBoxStrokePaint)
            val totLabelBaseline = pdfCenteredLabelFirstBaseline(
                totTop, totBot, totalLabelLines.size, totalStrongLh, fmTotalLabel
            )
            totalLabelLines.forEachIndexed { i, ln ->
                canvas.drawText(ln, totalsLeft + 20f, totLabelBaseline + i * totalStrongLh, strongPaint)
            }
            val totalAmtBaseline = pdfCenteredSingleLineBaseline(totTop, totBot, strongRightPaint.fontMetrics)
            canvas.drawText("₹${money(invoice.total)}", amountRightX, totalAmtBaseline, strongRightPaint)
            val totalBoxBottom = totBot
            val footerTextW = (contentRight - contentLeft - 12f).coerceAtLeast(120f)
            var belowTotalsY = totalBoxBottom + if (invoice.notes.isNotBlank()) 18f else 28f
            if (invoice.notes.isNotBlank()) {
                val notesTitlePaint = Paint(strongPaint).apply { textSize = 26f }
                belowTotalsY = drawPdfWrappedLeft(canvas, "Notes", notesTitlePaint, contentLeft, belowTotalsY, footerTextW)
                belowTotalsY += 6f
                belowTotalsY = drawPdfWrappedLeft(
                    canvas,
                    invoice.notes.trim(),
                    bodyPaint,
                    contentLeft,
                    belowTotalsY,
                    footerTextW,
                    tightTrailing = true
                )
                belowTotalsY += 4f
            }
            val gapBeforeFooterRule = if (invoice.notes.isNotBlank()) 8f else 20f
            var footerRuleY = belowTotalsY + gapBeforeFooterRule
            canvas.drawLine(contentLeft, footerRuleY, contentRight, footerRuleY, mutedLinePaint)

            val upiVpa = INVOICE_UPI_VPA
            val upiFooterPaint = Paint(strongPaint).apply {
                textSize = 26f
                color = themePrimary
            }
            val qrSizePts = 180f
            val footerBlockMinH = 38f + 220f + qrSizePts + 80f
            if (footerRuleY + footerBlockMinH > 1680f) {
                finishCurrentPage()
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(1240, 1754, pageNumber).create())
                canvas = page.canvas
                canvas.drawRect(0f, 0f, 1240f, 1754f, bodyPanelFillPaint)
                footerRuleY = 80f
                canvas.drawLine(contentLeft, footerRuleY, contentRight, footerRuleY, mutedLinePaint)
            }

            var footerY = footerRuleY + 38f
            footerY = drawPdfWrappedLeft(
                canvas,
                "Thank you for choosing Crumbs & Soul.",
                smallPaint,
                contentLeft,
                footerY,
                footerTextW
            )
            footerY += 20f

            val upiDeepLink = buildUpiDeepLink(upiVpa, INVOICE_UPI_PAYEE_NAME, invoice.total)
            val qrBitmapFull = createQrCodeBitmap(
                upiDeepLink,
                512,
                lightColor = themeBackground
            )
            // Align QR right edge with item table (same inset as line items).
            val qrLeft = tblRight - qrSizePts
            val upiColumnMaxW = (qrLeft - 20f - contentLeft).coerceAtLeast(100f)
            val upiBlockTop = footerY
            val upiTextEndY = drawPdfWrappedLeft(
                canvas,
                "Pay via UPI — $upiVpa\n" +
                    "Amount: ₹${money(invoice.total)}",
                upiFooterPaint,
                contentLeft,
                footerY,
                upiColumnMaxW
            )
            if (qrBitmapFull != null) {
                val scaled = Bitmap.createScaledBitmap(qrBitmapFull, qrSizePts.toInt(), qrSizePts.toInt(), true)
                if (scaled !== qrBitmapFull) qrBitmapFull.recycle()
                val dst = Rect(
                    qrLeft.toInt(),
                    upiBlockTop.toInt(),
                    (qrLeft + qrSizePts).toInt(),
                    (upiBlockTop + qrSizePts).toInt()
                )
                canvas.drawBitmap(scaled, null, dst, null)
                scaled.recycle()
            }
            footerY = maxOf(upiTextEndY, upiBlockTop + qrSizePts) + 16f
            val scanHintPaint = Paint(smallPaint).apply { textSize = 20f }
            footerY = drawPdfWrappedLeft(
                canvas,
                "Scan the QR with any UPI app to pay the amount above.",
                scanHintPaint,
                contentLeft,
                footerY,
                footerTextW
            )
            footerY += 8f
            drawPdfWrappedLeft(
                canvas,
                "This is a computer generated invoice. No signature required.",
                smallPaint,
                contentLeft,
                footerY,
                footerTextW
            )

            finishCurrentPage()
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
}

fun createSalesReportPdf(
    context: Context,
    reportType: String,
    monthFilter: String,
    customerFilter: String,
    rows: List<ReportRow>,
    records: List<InvoiceRecord>
): File? {
    return try {
        val document = PdfDocument()
        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(1240, 1754, pageNumber).create())
        var canvas = page.canvas
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
        val smallBoldPaint = Paint().apply {
            color = Color.parseColor("#111111")
            textSize = 20f
            isFakeBoldText = true
        }
        val smallBodyPaint = Paint().apply {
            color = Color.parseColor("#111111")
            textSize = 18f
        }
        val smallLh = paintLineHeight(smallBodyPaint)
        val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B4AE9E")
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }

        fun drawPageHeader(): Float {
            canvas.drawText("Crumbs & Soul - Sales Report", 70f, 100f, titlePaint)
            canvas.drawText("Type: $reportType", 70f, 150f, subtitlePaint)
            val filterLines = pdfWrapText(
                "Month: $monthFilter | Customer: $customerFilter",
                subtitlePaint,
                1100f
            )
            val subLh = paintLineHeight(subtitlePaint)
            var hy = 190f
            filterLines.forEachIndexed { i, line ->
                canvas.drawText(line, 70f, hy + i * subLh, subtitlePaint)
            }
            hy += filterLines.size * subLh + 14f
            canvas.drawLine(60f, hy, 1180f, hy, rulePaint)
            return hy + 28f
        }

        fun newPage() {
            document.finishPage(page)
            pageNumber += 1
            page = document.startPage(PdfDocument.PageInfo.Builder(1240, 1754, pageNumber).create())
            canvas = page.canvas
        }

        var y = drawPageHeader()
        val reportPageBottomY = 1700f

        val xNameCol = 70f
        val xCountCol = 730f
        val xTotalCol = 930f
        val maxSummaryNameW = (xCountCol - xNameCol - 24f).coerceAtLeast(120f)
        val summaryBodyLh = paintLineHeight(bodyPaint)

        // Keep summary column headers + at least one full data row on the same page.
        val summaryHeaderBandH = 28f + 36f
        val minSummaryDataRowH = summaryBodyLh + 12f
        if (y + summaryHeaderBandH + minSummaryDataRowH > reportPageBottomY) {
            newPage()
            y = drawPageHeader()
        }

        fun drawSummaryTableHeaders(atY: Float): Float {
            var yy = atY
            canvas.drawText(if (reportType == "Month-wise") "Month" else "Customer", 70f, yy, boldPaint)
            canvas.drawText("Invoices", 700f, yy, boldPaint)
            canvas.drawText("Total Sales", 930f, yy, boldPaint)
            yy += 28f
            canvas.drawLine(60f, yy, 1180f, yy, rulePaint)
            yy += 36f
            return yy
        }

        y = drawSummaryTableHeaders(y)

        rows.forEach { row ->
            val nameLines = pdfWrapText(row.key, bodyPaint, maxSummaryNameW)
            val rowBlockH = nameLines.size * summaryBodyLh + 12f
            if (y + rowBlockH > reportPageBottomY) {
                newPage()
                y = drawPageHeader()
                y = drawSummaryTableHeaders(y)
            }
            val rowTop = y
            nameLines.forEachIndexed { i, line ->
                canvas.drawText(line, xNameCol, rowTop + i * summaryBodyLh, bodyPaint)
            }
            canvas.drawText(row.invoiceCount.toString(), xCountCol, rowTop, bodyPaint)
            canvas.drawText("₹${money(row.totalSales)}", xTotalCol, rowTop, bodyPaint)
            y = rowTop + rowBlockH
        }

        y += 20f
        canvas.drawLine(60f, y, 1180f, y, rulePaint)
        y += 40f
        val grandLines = pdfWrapText(
            "Grand Total: ₹${money(rows.sumOf { it.totalSales })}",
            boldPaint,
            1050f
        )
        val grandLh = paintLineHeight(boldPaint)
        if (y + grandLines.size * grandLh > reportPageBottomY - 40f) {
            newPage()
            y = drawPageHeader()
        }
        grandLines.forEachIndexed { i, line ->
            canvas.drawText(line, 70f, y + i * grandLh, boldPaint)
        }
        y += grandLines.size * grandLh + 36f
        val genLines = pdfWrapText(
            "Generated on: ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())}",
            subtitlePaint,
            1100f
        )
        val genLh = paintLineHeight(subtitlePaint)
        if (y + genLines.size * genLh > reportPageBottomY) {
            newPage()
            y = drawPageHeader()
        }
        genLines.forEachIndexed { i, line ->
            canvas.drawText(line, 70f, y + i * genLh, subtitlePaint)
        }
        y += genLines.size * genLh + 32f

        val invoiceRows = buildInvoicePdfRows(records)
        val xInv = 62f
        val xType = 188f
        val xStatus = 258f
        val xCust = 348f
        val xDate = 538f
        val rQty = 758f
        val rShip = 898f
        val rTotal = 1148f
        val wInv = (xType - xInv - 8f).coerceAtLeast(55f)
        val wType = (xStatus - xType - 8f).coerceAtLeast(36f)
        val wStatus = (xCust - xStatus - 8f).coerceAtLeast(52f)
        val wCust = (xDate - xCust - 10f).coerceAtLeast(72f)
        val wDate = (rQty - xDate - 14f).coerceAtLeast(56f)
        val qtyRightPaint = Paint(smallBodyPaint).apply { textAlign = Paint.Align.RIGHT }
        val shipRightPaint = Paint(smallBodyPaint).apply { textAlign = Paint.Align.RIGHT }
        val totalRightPaint = Paint(smallBodyPaint).apply { textAlign = Paint.Align.RIGHT }
        val hdrQtyRight = Paint(smallBoldPaint).apply { textAlign = Paint.Align.RIGHT }
        val hdrShipRight = Paint(smallBoldPaint).apply { textAlign = Paint.Align.RIGHT }
        val hdrTotalRight = Paint(smallBoldPaint).apply { textAlign = Paint.Align.RIGHT }

        fun detailRowHeight(row: ReportInvoicePdfRow): Float {
            val invLines = pdfWrapText(row.invoiceNumber, smallBodyPaint, wInv)
            val typeLines = pdfWrapText(row.invoiceType, smallBodyPaint, wType)
            val statLines = pdfWrapText(row.invoiceStatus, smallBodyPaint, wStatus)
            val custLines = pdfWrapText(row.customerName, smallBodyPaint, wCust)
            val dateLines = pdfWrapText(row.invoiceDate, smallBodyPaint, wDate)
            val maxTextLines = maxOf(
                invLines.size,
                typeLines.size,
                statLines.size,
                custLines.size,
                dateLines.size,
                1
            )
            return maxTextLines * smallLh + 10f
        }

        // Title + rule + column headers + first data row on one page (no orphan headers/columns).
        val detailIntroH = 32f + 36f + 48f
        val firstDetailRowH =
            invoiceRows.firstOrNull()?.let { detailRowHeight(it) } ?: (smallLh + 10f)
        if (y + detailIntroH + firstDetailRowH > reportPageBottomY) {
            newPage()
            y = drawPageHeader()
        }
        canvas.drawText("Invoice-wise Details", 70f, y, boldPaint)
        y += 32f
        canvas.drawLine(60f, y, 1180f, y, rulePaint)
        y += 36f

        fun drawDetailsHeader() {
            canvas.drawText("Inv#", xInv, y, smallBoldPaint)
            canvas.drawText("Type", xType, y, smallBoldPaint)
            canvas.drawText("Status", xStatus, y, smallBoldPaint)
            canvas.drawText("Customer", xCust, y, smallBoldPaint)
            canvas.drawText("Date", xDate, y, smallBoldPaint)
            canvas.drawText("Qty", rQty, y, hdrQtyRight)
            canvas.drawText("Ship", rShip, y, hdrShipRight)
            canvas.drawText("Total", rTotal, y, hdrTotalRight)
            y += 20f
            canvas.drawLine(60f, y, 1180f, y, rulePaint)
            y += 28f
        }

        fun startDetailsContinuation(title: String) {
            newPage()
            y = drawPageHeader()
            canvas.drawText(title, 70f, y, boldPaint)
            y += 32f
            canvas.drawLine(60f, y, 1180f, y, rulePaint)
            y += 36f
            drawDetailsHeader()
        }

        drawDetailsHeader()
        invoiceRows.forEach { row ->
            val invLines = pdfWrapText(row.invoiceNumber, smallBodyPaint, wInv)
            val typeLines = pdfWrapText(row.invoiceType, smallBodyPaint, wType)
            val statLines = pdfWrapText(row.invoiceStatus, smallBodyPaint, wStatus)
            val custLines = pdfWrapText(row.customerName, smallBodyPaint, wCust)
            val dateLines = pdfWrapText(row.invoiceDate, smallBodyPaint, wDate)
            val maxTextLines = maxOf(
                invLines.size,
                typeLines.size,
                statLines.size,
                custLines.size,
                dateLines.size,
                1
            )
            val rowH = maxTextLines * smallLh + 10f
            if (y + rowH > reportPageBottomY) {
                // Only move to a new page if the full row fits below headers there (avoid infinite continuation).
                val minDataTopOnNewPage = 320f
                if (rowH <= reportPageBottomY - minDataTopOnNewPage) {
                    startDetailsContinuation("Invoice-wise Details (contd.)")
                }
            }
            val rowTop = y
            for (i in 0 until maxTextLines) {
                val bl = rowTop + i * smallLh
                if (i < invLines.size) canvas.drawText(invLines[i], xInv, bl, smallBodyPaint)
                if (i < typeLines.size) canvas.drawText(typeLines[i], xType, bl, smallBodyPaint)
                if (i < statLines.size) canvas.drawText(statLines[i], xStatus, bl, smallBodyPaint)
                if (i < custLines.size) canvas.drawText(custLines[i], xCust, bl, smallBodyPaint)
                if (i < dateLines.size) canvas.drawText(dateLines[i], xDate, bl, smallBodyPaint)
            }
            canvas.drawText(row.totalItemQty.toString(), rQty, rowTop, qtyRightPaint)
            canvas.drawText(money(row.shippingCharges), rShip, rowTop, shipRightPaint)
            canvas.drawText(money(row.invoiceTotal), rTotal, rowTop, totalRightPaint)
            y = rowTop + rowH
        }

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

@Suppress("UNUSED_PARAMETER")
fun createSalesReportExcel(
    context: Context,
    reportType: String,
    monthFilter: String,
    customerFilter: String,
    rows: List<ReportRow>,
    records: List<InvoiceRecord>,
    customers: List<Customer>
): File? {
    return try {
        val workbook = XSSFWorkbook()
        val generatedOn = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())

        // Sheet 1 — mirrors PDF: summary table + invoice-wise details (same columns as PDF).
        val sheet1 = workbook.createSheet("Sales Report")
        var rowIndex = 0
        sheet1.createRow(rowIndex++).apply { createCell(0).setCellValue("Crumbs & Soul - Sales Report") }
        sheet1.createRow(rowIndex++).apply { createCell(0).setCellValue("Type: $reportType") }
        sheet1.createRow(rowIndex++).apply {
            createCell(0).setCellValue("Month: $monthFilter | Customer: $customerFilter")
        }
        rowIndex++
        sheet1.createRow(rowIndex++).apply {
            createCell(0).setCellValue(if (reportType == "Month-wise") "Month" else "Customer")
            createCell(1).setCellValue("Invoices")
            createCell(2).setCellValue("Total Sales (₹)")
        }
        rows.forEach {
            sheet1.createRow(rowIndex++).apply {
                createCell(0).setCellValue(it.key)
                createCell(1).setCellValue(it.invoiceCount.toDouble())
                createCell(2).setCellValue(it.totalSales)
            }
        }
        sheet1.createRow(rowIndex++).apply {
            createCell(0).setCellValue("Grand Total")
            createCell(2).setCellValue(rows.sumOf { it.totalSales })
        }
        sheet1.createRow(rowIndex++).apply {
            createCell(0).setCellValue("Generated on: $generatedOn")
        }
        rowIndex++
        sheet1.createRow(rowIndex++).apply { createCell(0).setCellValue("Invoice-wise Details") }
        sheet1.createRow(rowIndex++).apply {
            createCell(0).setCellValue("Inv#")
            createCell(1).setCellValue("Type")
            createCell(2).setCellValue("Status")
            createCell(3).setCellValue("Customer")
            createCell(4).setCellValue("Date")
            createCell(5).setCellValue("Qty")
            createCell(6).setCellValue("Ship (₹)")
            createCell(7).setCellValue("Total (₹)")
        }
        buildInvoicePdfRows(records).forEach { inv ->
            sheet1.createRow(rowIndex++).apply {
                createCell(0).setCellValue(inv.invoiceNumber)
                createCell(1).setCellValue(inv.invoiceType)
                createCell(2).setCellValue(inv.invoiceStatus)
                createCell(3).setCellValue(inv.customerName)
                createCell(4).setCellValue(inv.invoiceDate)
                createCell(5).setCellValue(inv.totalItemQty.toDouble())
                createCell(6).setCellValue(inv.shippingCharges)
                createCell(7).setCellValue(inv.invoiceTotal)
            }
        }
        sheet1.safeAutoSizeColumnsInclusive(7)

        // Sheet 2 — item-wise sales with B2B / B2C split.
        val sheet2 = workbook.createSheet("Item Sales B2B B2C")
        var r2 = 0
        sheet2.createRow(r2++).apply {
            createCell(0).setCellValue("Item")
            createCell(1).setCellValue("B2B Qty")
            createCell(2).setCellValue("B2B Sales (₹)")
            createCell(3).setCellValue("B2C Qty")
            createCell(4).setCellValue("B2C Sales (₹)")
            createCell(5).setCellValue("Total Qty")
            createCell(6).setCellValue("Total Sales (₹)")
        }
        buildItemSalesB2bB2cRows(records).forEach { (name, agg) ->
            sheet2.createRow(r2++).apply {
                createCell(0).setCellValue(name)
                createCell(1).setCellValue(agg.b2bQty.toDouble())
                createCell(2).setCellValue(agg.b2bSales)
                createCell(3).setCellValue(agg.b2cQty.toDouble())
                createCell(4).setCellValue(agg.b2cSales)
                createCell(5).setCellValue(agg.totalQty.toDouble())
                createCell(6).setCellValue(agg.totalSales)
            }
        }
        sheet2.safeAutoSizeColumnsInclusive(6)

        // Sheet 3 — customer-wise sales (name + phone bucket).
        val sheet3 = workbook.createSheet("Customer Sales")
        var r3 = 0
        sheet3.createRow(r3++).apply {
            createCell(0).setCellValue("Customer")
            createCell(1).setCellValue("Phone")
            createCell(2).setCellValue("Invoices")
            createCell(3).setCellValue("Total Sales (₹)")
        }
        buildCustomerSalesExcelRows(records).forEach { c ->
            sheet3.createRow(r3++).apply {
                createCell(0).setCellValue(c.customerName)
                createCell(1).setCellValue(c.customerPhone)
                createCell(2).setCellValue(c.invoiceCount.toDouble())
                createCell(3).setCellValue(c.totalSales)
            }
        }
        sheet3.safeAutoSizeColumnsInclusive(3)

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

fun sendGoogleReviewWhatsApp(context: Context, customerPhone: String) {
    val normalized = normalizeWhatsAppPhone(customerPhone)
    if (normalized == null) {
        Toast.makeText(context, "Invalid phone number for WhatsApp", Toast.LENGTH_SHORT).show()
        return
    }
    val message = buildString {
        appendLine("Hi! Thank you for choosing Crumbs & Soul.")
        appendLine()
        appendLine(GOOGLE_REVIEW_SHARE_URL)
        appendLine()
        appendLine(
            "Kindly share your valuable feedback and rating in the above link with specific about what you " +
                "really like about our services and also your favourite snacks ! This small step from you " +
                "would help us grow  bigger ! ✨🌿"
        )
        appendLine()
        appendLine("Cheers ✨")
    }
    val encoded = Uri.encode(message)
    val url = "https://wa.me/$normalized?text=$encoded"
    val uri = Uri.parse(url)
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, uri).apply { `package` = "com.whatsapp" }
        )
    } catch (_: Exception) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri).apply { `package` = "com.whatsapp.w4b" }
            )
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (_: Exception) {
                Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * Opens WhatsApp to [customerPhone] with a drafted message sharing the menu catalog link
 * ([WHATSAPP_MENU_CATALOG_URL]).
 */
fun sendMenuCatalogWhatsApp(context: Context, customerPhone: String, customerDisplayName: String) {
    val normalized = normalizeWhatsAppPhone(customerPhone)
    if (normalized == null) {
        Toast.makeText(context, "Invalid phone number for WhatsApp", Toast.LENGTH_SHORT).show()
        return
    }
    val greetingName = customerDisplayName.trim().ifBlank { "there" }
    val message = buildString {
        appendLine("Hi $greetingName!")
        appendLine()
        appendLine("Here's our menu catalog on WhatsApp — browse and order anytime:")
        appendLine(WHATSAPP_MENU_CATALOG_URL)
        appendLine()
        appendLine("— Crumbs & Soul")
    }
    val encoded = Uri.encode(message)
    val url = "https://wa.me/$normalized?text=$encoded"
    val uri = Uri.parse(url)
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, uri).apply { `package` = "com.whatsapp" }
        )
    } catch (_: Exception) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri).apply { `package` = "com.whatsapp.w4b" }
            )
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (_: Exception) {
                Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

fun sendPaymentReminderWhatsApp(
    context: Context,
    customerPhone: String,
    invoiceNumber: String,
    invoiceDate: String,
    amountRupee: String
) {
    val normalized = normalizeWhatsAppPhone(customerPhone)
    if (normalized == null) {
        Toast.makeText(context, "Invalid phone number for WhatsApp", Toast.LENGTH_SHORT).show()
        return
    }
    val message = buildString {
        appendLine("Dear Customer,")
        appendLine()
        appendLine("This is a gentle reminder regarding your pending payment for Crumbs & Soul.")
        appendLine()
        appendLine("Invoice No: $invoiceNumber")
        appendLine("Date: $invoiceDate")
        appendLine("Amount due: ₹$amountRupee")
        appendLine()
        appendLine("Thank you,")
        appendLine("Crumbs & Soul")
    }
    val encoded = Uri.encode(message)
    val url = "https://wa.me/$normalized?text=$encoded"
    val uri = Uri.parse(url)
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, uri).apply { `package` = "com.whatsapp" }
        )
    } catch (_: Exception) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri).apply { `package` = "com.whatsapp.w4b" }
            )
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (_: Exception) {
                Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * Drafted message for WhatsApp when sharing the invoice PDF from the Generate screen (PDF includes UPI QR).
 */
private fun buildInvoiceWhatsAppShareMessage(invoice: InvoiceData): String {
    val name = invoice.customerName.trim().ifBlank { "there" }
    val invType = normalizeInvoiceType(invoice.invoiceType)
    return buildString {
        appendLine("Hi $name,")
        appendLine()
        appendLine("Please find your invoice (PDF) and UPI payment QR below. Amount is prefilled when you scan the QR.")
        appendLine()
        appendLine("Invoice No: ${invoice.invoiceNumber}")
        appendLine("Date: ${invoice.invoiceDate}")
        appendLine("Type: $invType")
        appendLine("Total: ₹${money(invoice.total)}")
        appendLine()
        if (invoice.notes.isNotBlank()) {
            appendLine("Notes:")
            appendLine(invoice.notes.trim())
            appendLine()
        }
        appendLine("UPI ID: $INVOICE_UPI_VPA")
        appendLine()
        appendLine("— Crumbs & Soul")
    }
}

/**
 * Shares the invoice PDF to the customer on WhatsApp with a drafted message.
 * (The PDF already includes a UPI QR; a separate PNG is not attached because WhatsApp often drops the PDF
 * when using [Intent.ACTION_SEND_MULTIPLE] with mixed types.)
 */
fun shareInvoicePdfAndPaymentQrOnWhatsApp(
    context: Context,
    pdfFile: File,
    invoice: InvoiceData,
    customerPhone: String
) {
    val normalized = normalizeWhatsAppPhone(customerPhone)
    if (normalized == null) {
        Toast.makeText(context, "Invalid customer phone for WhatsApp", Toast.LENGTH_SHORT).show()
        return
    }
    val jid = "$normalized@s.whatsapp.net"
    val message = buildInvoiceWhatsAppShareMessage(invoice)
    val pdfUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        pdfFile
    )
    launchWhatsAppPdfOnly(context, pdfUri, jid, message)
}

/** Opens WhatsApp to send a single invoice PDF; [caption] is prefilled as the chat message when non-null. */
private fun launchWhatsAppPdfOnly(context: Context, pdfUri: Uri, jid: String, caption: String? = null) {
    fun buildSendIntent(targetPackage: String?): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            clipData = ClipData.newUri(context.contentResolver, "application/pdf", pdfUri)
            if (caption != null) putExtra(Intent.EXTRA_TEXT, caption)
            putExtra("jid", jid)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (targetPackage != null) `package` = targetPackage
        }

    grantReadToWhatsAppPackages(context, listOf(pdfUri))
    try {
        context.startActivity(buildSendIntent("com.whatsapp"))
    } catch (_: Exception) {
        try {
            context.startActivity(buildSendIntent("com.whatsapp.w4b"))
        } catch (_: Exception) {
            Toast.makeText(context, "WhatsApp not found. Opening share sheet.", Toast.LENGTH_SHORT).show()
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                clipData = ClipData.newUri(context.contentResolver, "application/pdf", pdfUri)
                if (caption != null) putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(fallback, "Share invoice"))
        }
    }
}

private fun invoiceDataFromRecord(record: InvoiceRecord): InvoiceData =
    InvoiceData(
        invoiceNumber = record.invoiceNumber,
        invoiceDate = record.invoiceDate,
        customerName = record.customerName,
        customerPhone = record.customerPhone,
        items = record.items,
        shippingCharges = record.shippingCharges,
        total = record.total,
        invoiceType = record.invoiceType,
        notes = record.notes
    )

/** Message when sharing only the payment QR (e.g. from invoice history). */
private fun buildPaymentQrOnlyShareMessage(invoice: InvoiceData): String {
    val name = invoice.customerName.trim().ifBlank { "there" }
    val invType = normalizeInvoiceType(invoice.invoiceType)
    return buildString {
        appendLine("Hi $name,")
        appendLine()
        appendLine("Here is your UPI payment QR for this invoice. Scan to pay the amount (prefilled).")
        appendLine()
        appendLine("Invoice No: ${invoice.invoiceNumber}")
        appendLine("Date: ${invoice.invoiceDate}")
        appendLine("Type: $invType")
        appendLine("Total: ₹${money(invoice.total)}")
        appendLine()
        appendLine("UPI ID: $INVOICE_UPI_VPA")
        appendLine()
        appendLine("— Crumbs & Soul")
    }
}

/**
 * Shares only the UPI payment QR image (PNG) for [invoice] with a drafted WhatsApp message.
 */
fun sharePaymentQrOnlyOnWhatsApp(context: Context, invoice: InvoiceData, customerPhone: String) {
    val normalized = normalizeWhatsAppPhone(customerPhone)
    if (normalized == null) {
        Toast.makeText(context, "Invalid phone number for WhatsApp", Toast.LENGTH_SHORT).show()
        return
    }
    val jid = "$normalized@s.whatsapp.net"
    val upiLink = buildUpiDeepLink(INVOICE_UPI_VPA, INVOICE_UPI_PAYEE_NAME, invoice.total)
    val qrBitmap = createQrCodeBitmap(upiLink, 640, lightColor = invoiceQrLightBackgroundColor)
    val folder = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
        "CrumbsAndSoulInvoices"
    ).apply { mkdirs() }
    val qrFile = File(folder, "Invoice_${invoice.invoiceNumber}_pay_qr.png")
    var wroteQrFile = false
    if (qrBitmap != null) {
        try {
            FileOutputStream(qrFile).use { out ->
                wroteQrFile = qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } finally {
            if (!qrBitmap.isRecycled) qrBitmap.recycle()
        }
    }
    if (!wroteQrFile || !qrFile.isFile || qrFile.length() == 0L) {
        Toast.makeText(context, "Could not create payment QR", Toast.LENGTH_SHORT).show()
        return
    }
    val qrUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        qrFile
    )
    val message = buildPaymentQrOnlyShareMessage(invoice)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, qrUri)
        putExtra(Intent.EXTRA_TEXT, message)
        putExtra("jid", jid)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        `package` = "com.whatsapp"
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            context.startActivity(intent.apply { `package` = "com.whatsapp.w4b" })
        } catch (_: Exception) {
            Toast.makeText(context, "WhatsApp not found. Opening share sheet.", Toast.LENGTH_SHORT).show()
            try {
                val fallback = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, qrUri)
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(fallback, "Share payment QR"))
            } catch (_: Exception) {
                Toast.makeText(context, "Could not share payment QR", Toast.LENGTH_SHORT).show()
            }
        }
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

fun openExportedFile(context: Context, file: File) {
    val extension = file.extension.lowercase(Locale.getDefault())
    val mimeType = when (extension) {
        "pdf" -> "application/pdf"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        else -> "*/*"
    }
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(viewIntent)
    } catch (_: Exception) {
        Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
    }
}

fun shareAnyFile(context: Context, file: File, mimeType: String, chooserTitle: String = "Share file") {
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
    context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
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
