package com.example

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) downloadDir.mkdirs()

            // 1. Format A - Detailed Rec Legal Portrait
            val fileA = File(downloadDir, "Detailed Rec $monthYear Legal Portrait.xlsx")
            val wbA = XSSFWorkbook()
            val sheetA = wbA.createSheet("Detailed Rec")
            val rowA0 = sheetA.createRow(0)
            rowA0.createCell(0).setCellValue("OFFICE OF THE PRESIDING OFFICER LABOUR COURT HARIPUR")
            val rowA1 = sheetA.createRow(1)
            rowA1.createCell(0).setCellValue("GRANT NO. 21037 (030) - HR-4007 | FOR: $monthYear")
            
            val headersA = listOf("Object Head", "Budget Estimate 2026-2027", "Revised Budget", "Actual Exp ($monthYear)", "Exp B/F", "Progressive Exp")
            val hRowA = sheetA.createRow(3)
            headersA.forEachIndexed { i, h -> hRowA.createCell(i).setCellValue(h) }
            
            FileOutputStream(fileA).use { wbA.write(it) }
            wbA.close()

            // 2. Format B - AG Rec Landscape
            val fileB = File(downloadDir, "Monthly Expenditure Statement $monthYear Landscape.xlsx")
            val wbB = XSSFWorkbook()
            val sheetB = wbB.createSheet("AG Rec")
            val rowB0 = sheetB.createRow(0)
            rowB0.createCell(0).setCellValue("MONTHLY EXPENDITURE STATEMENT & RECONCILIATION WITH AG FIGURES")
            val rowB1 = sheetB.createRow(1)
            rowB1.createCell(0).setCellValue("OFFICE OF THE PRESIDING OFFICER, LABOUR COURT HARIPUR ($monthYear)")
            
            val headersB = listOf("Major / Minor Head", "Budget 2026-27", "Revised Budget", "Dept Figures (Month)", "Dept Progressive", "AG Figures (Month)", "AG Progressive", "Variation")
            val hRowB = sheetB.createRow(3)
            headersB.forEachIndexed { i, h -> hRowB.createCell(i).setCellValue(h) }

            FileOutputStream(fileB).use { wbB.write(it) }
            wbB.close()

            // 3. Format C - RECONCILED Covering Letter Docx
            val fileC = File(downloadDir, "RECONCILED Covering Letter $monthYear.docx")
            val doc = XWPFDocument()
            val titlePara = doc.createParagraph()
            titlePara.alignment = ParagraphAlignment.CENTER
            val titleRun = titlePara.createRun()
            titleRun.isBold = true
            titleRun.fontSize = 14
            titleRun.setText("OFFICE OF THE PRESIDING OFFICER, LABOUR COURT HARIPUR\n\n")

            val bodyPara = doc.createParagraph()
            val bodyRun = bodyPara.createRun()
            bodyRun.fontSize = 12
            bodyRun.setText("SUBJECT: MONTHLY RECONCILIATION STATEMENT (EXPENDITURE) FOR $monthYear.\n\n")
            bodyRun.setText("Enclosed find herewith the monthly reconciliation statement (Expenditure) for the month of $monthYear, duly verified by District Accounts Officer Haripur for further necessary action, please.\n\n\n")
            bodyRun.setText("District & Sessions Judge / Presiding Officer\nLabour Court Haripur")

            FileOutputStream(fileC).use { doc.write(it) }
            doc.close()

            Toast.makeText(this, "Success!\nAll 3 files saved in Downloads:\n1. Format A (Detailed Rec)\n2. Format B (AG Rec)\n3. Format C (Covering Letter)", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
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
    var selectedMonth by remember { mutableStateOf(months[3]) } // Default October 2026
    var statusText by remember { mutableStateOf("Ready to generate") }

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
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
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
                        label = { Text("Select Reconciliation Month") },
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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        statusText = "Generated for $selectedMonth!"
                        onGenerate(selectedMonth)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                ) {
                    Text(
                        text = "Generate All 3 Formats",
                        fontSize = 16.sp,
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
