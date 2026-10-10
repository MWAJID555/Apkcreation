package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF4F6F9)
                ) {
                    ReconciliationScreen(
                        onGenerate = { month ->
                            generateReconciliationFiles(month)
                        }
                    )
                }
            }
        }
    }

    private fun generateReconciliationFiles(monthYear: String) {
        try {
            // محفوظ انٹرنل اسٹوریج (جہاں کسی پرمیشن کی ضرورت نہیں ہوتی اور ایپ کریش نہیں ہوتی)
            val outputDir = File(getExternalFilesDir(null), "Reconciliation")
            if (!outputDir.exists()) outputDir.mkdirs()

            // 1. Format A: Detailed Rec
            val fileA = File(outputDir, "Detailed Rec $monthYear Legal Portrait.xlsx")
            val wbA = XSSFWorkbook()
            val sheetA = wbA.createSheet("Detailed Rec")
            val r0A = sheetA.createRow(0)
            r0A.createCell(0).setCellValue("OF THE OFFICE OF PRESIDING OFFICER LABOUR COURT HARIPUR")
            val r1A = sheetA.createRow(1)
            r1A.createCell(0).setCellValue("GRANT NO. 21037 (030) | FOR: $monthYear")
            
            val headersA = listOf("Object Head", "Budget Estimate 2026-2027", "Revised Budget", "Actual Exp ($monthYear)", "Exp B/F", "Progressive Exp")
            val hRowA = sheetA.createRow(3)
            headersA.forEachIndexed { i, h -> hRowA.createCell(i).setCellValue(h) }
            
            val headsA = listOf(
                "A-01 TOTAL EMPLOYEES RELATED EXPENSES",
                "A-011 PAY",
                "A-012 REGULAR ALLOWANCES",
                "A-03 TOTAL OPERATING EXPENSES",
                "A-03805 Travelling Allowance",
                "A-09 TOTAL PHYSICAL ASSETS",
                "A-13 TOTAL REPAIRS AND MAINTENANCE"
            )
            headsA.forEachIndexed { idx, head ->
                val r = sheetA.createRow(4 + idx)
                r.createCell(0).setCellValue(head)
                r.createCell(1).setCellValue(0.0)
                r.createCell(2).setCellValue(0.0)
                r.createCell(3).setCellValue(0.0)
                r.createCell(4).setCellValue(0.0)
                r.createCell(5).cellFormula = "D${5 + idx}+E${5 + idx}"
            }
            FileOutputStream(fileA).use { wbA.write(it) }
            wbA.close()

            // 2. Format B: AG Rec
            val fileB = File(outputDir, "Monthly Expenditure Statement $monthYear Landscape.xlsx")
            val wbB = XSSFWorkbook()
            val sheetB = wbB.createSheet("AG Rec")
            val r0B = sheetB.createRow(0)
            r0B.createCell(0).setCellValue("MONTHLY EXPENDITURE STATEMENT & RECONCILIATION WITH AG FIGURES")
            val r1B = sheetB.createRow(1)
            r1B.createCell(0).setCellValue("LABOUR COURT HARIPUR - $monthYear")
            
            val headersB = listOf("Major / Minor Head", "Budget 2026-27", "Revised Budget", "Dept Month", "Dept Progressive", "AG Month", "AG Progressive", "Variation")
            val hRowB = sheetB.createRow(3)
            headersB.forEachIndexed { i, h -> hRowB.createCell(i).setCellValue(h) }
            
            val headsB = listOf(
                "A-01 Employees Related Expenses",
                "A-03 Operating Expenses",
                "A-04 Transfers",
                "A-05 Grants",
                "A-09 Physical Assets",
                "A-13 Repairs and Maintenance",
                "GRAND TOTAL"
            )
            headsB.forEachIndexed { idx, head ->
                val r = sheetB.createRow(4 + idx)
                r.createCell(0).setCellValue(head)
                for (c in 1..6) r.createCell(c).setCellValue(0.0)
                r.createCell(7).cellFormula = "E${5 + idx}-G${5 + idx}"
            }
            FileOutputStream(fileB).use { wbB.write(it) }
            wbB.close()

            // 3. Format C: Covering Letter
            val fileC = File(outputDir, "RECONCILED Covering Letter $monthYear.docx")
            val doc = XWPFDocument()
            val p1 = doc.createParagraph()
            p1.alignment = ParagraphAlignment.CENTER
            val r1 = p1.createRun()
            r1.isBold = true
            r1.setText("OFFICE OF THE PRESIDING OFFICER LABOUR COURT HARIPUR\n\n")

            val p2 = doc.createParagraph()
            val r2 = p2.createRun()
            r2.setText("SUBJECT: MONTHLY RECONCILIATION STATEMENT (EXPENDITURE) FOR $monthYear.\n\n")
            r2.setText("With reference to subject noted above, enclosed find herewith the monthly reconciliation statement (Expenditure) for the month of $monthYear, duly verified by District Account Officer Haripur for further necessary action, please.\n\n")
            r2.setText("D&SJ/Presiding Officer\nLabour Court Haripur")

            FileOutputStream(fileC).use { doc.write(it) }
            doc.close()

            // ڈاؤن لوڈز فولڈر میں کاپی کریں تاکہ فائل مینیجر میں بھی نظر آئے
            try {
                val publicDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                fileA.copyTo(File(publicDownload, fileA.name), overwrite = true)
                fileB.copyTo(File(publicDownload, fileB.name), overwrite = true)
                fileC.copyTo(File(publicDownload, fileC.name), overwrite = true)
            } catch (_: Exception) {}

            Toast.makeText(this, "Success! 3 Files Created Successfully!", Toast.LENGTH_LONG).show()

        } catch (e: Throwable) {
            Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReconciliationScreen(onGenerate: (String) -> Unit) {
    val months = listOf(
        "July 2026", "August 2026", "September 2026", "October 2026",
        "November 2026", "December 2026", "January 2027", "February 2027",
        "March 2027", "April 2027", "May 2027", "June 2027"
    )
    var expanded by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf("October 2026") }
    var statusText by remember { mutableStateOf("Ready to generate official statements") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Labour Court Haripur",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B3B2B),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Monthly Expenditure Reconciliation Generator",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedMonth,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Financial Month") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(month) },
                                onClick = {
                                    selectedMonth = month
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        statusText = "Generating files for $selectedMonth..."
                        onGenerate(selectedMonth)
                        statusText = "Files Created for $selectedMonth!"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                ) {
                    Text(
                        text = "Generate All 3 Formats",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
