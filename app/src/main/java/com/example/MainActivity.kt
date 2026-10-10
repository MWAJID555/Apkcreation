package com.example

import android.os.Bundle
import android.os.Environment
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
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
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

            // ==========================================
            // 1. FORMAT A: Detailed Rec (Legal Portrait, 12pt)
            // ==========================================
            val fileA = File(downloadDir, "Detailed Rec $monthYear Legal Portrait.xlsx")
            val wbA = XSSFWorkbook()
            val sheetA = wbA.createSheet("Detailed Rec")
            
            val psA = sheetA.printSetup
            psA.paperSize = PrintSetup.LEGAL_PAPERSIZE
            psA.fitWidth = 1.toShort()
            psA.fitHeight = 2.toShort()
            sheetA.autobreaks = false

            val fontA = wbA.createFont().apply {
                fontName = "Book Antiqua"
                fontHeightInPoints = 12.toShort()
                color = IndexedColors.BLACK.index
            }
            val fontABold = wbA.createFont().apply {
                fontName = "Book Antiqua"
                fontHeightInPoints = 12.toShort()
                bold = true
                color = IndexedColors.BLACK.index
            }

            val styleA = wbA.createCellStyle().apply {
                setFont(fontA)
                borderTop = BorderStyle.THIN
                borderBottom = BorderStyle.THIN
                borderLeft = BorderStyle.THIN
                borderRight = BorderStyle.THIN
            }
            val styleABold = wbA.createCellStyle().apply {
                setFont(fontABold)
                borderTop = BorderStyle.THIN
                borderBottom = BorderStyle.THIN
                borderLeft = BorderStyle.THIN
                borderRight = BorderStyle.THIN
            }

            // Headers
            val r0A = sheetA.createRow(0)
            r0A.createCell(0).apply { setCellValue("OF THE OFFICE OF PRESIDING OFFICER LABOUR COURT HARIPUR"); cellStyle = styleABold }
            val r1A = sheetA.createRow(1)
            r1A.createCell(0).apply { setCellValue("GRANT NO. 21037 (030) | FINANCIAL YEAR 2026-2027"); cellStyle = styleABold }
            val r2A = sheetA.createRow(2)
            r2A.createCell(0).apply { setCellValue("UNDER HEADS 03- PUBLIC ORDER & SAFETY AFFAIRS, 031-LAW COURTS 031101 COURTS/ JUSTICE HR-4007 - LABOUR COURTS"); cellStyle = styleABold }
            val r3A = sheetA.createRow(3)
            r3A.createCell(0).apply { setCellValue("DETAILED EXPENDITURE STATEMENT FOR THE MONTH OF $monthYear"); cellStyle = styleABold }

            val colHeadersA = listOf(
                "Object Head", "Budget Estimate 2026-2027", "Revised Budget Estimate",
                "Actual Expenditure [$monthYear]", "Expenditure B/F", "Progressive Expenditure"
            )
            val hRowA = sheetA.createRow(5)
            colHeadersA.forEachIndexed { i, h ->
                hRowA.createCell(i).apply { setCellValue(h); cellStyle = styleABold }
            }

            // Data rows sample structure
            val sampleDataA = listOf(
                listOf("A-01 TOTAL EMPLOYEES RELATED EXPENSES", "0", "0", "0", "0"),
                listOf("A-011 PAY", "0", "0", "0", "0"),
                listOf("A-01101 Basic Pay of Officers", "0", "0", "0", "0"),
                listOf("A-01151 Basic Pay of Other Staff", "0", "0", "0", "0"),
                listOf("A-012 REGULAR ALLOWANCES", "0", "0", "0", "0"),
                listOf("A-03 TOTAL OPERATING EXPENSES", "0", "0", "0", "0"),
                listOf("A-03201 Postage and Telegraph", "0", "0", "0", "0"),
                listOf("A-03303 Electricity", "0", "0", "0", "0"),
                listOf("A-03805 Travelling Allowance", "0", "0", "0", "0"),
                listOf("A-09 TOTAL PHYSICAL ASSETS", "0", "0", "0", "0"),
                listOf("A-13 TOTAL REPAIRS AND MAINTENANCE", "0", "0", "0", "0")
            )

            var rowIdxA = 6
            sampleDataA.forEach { rowData ->
                val r = sheetA.createRow(rowIdxA)
                r.createCell(0).apply { setCellValue(rowData[0]); cellStyle = if (rowData[0].contains("TOTAL")) styleABold else styleA }
                for (c in 1..4) {
                    r.createCell(c).apply { setCellValue(rowData[c].toDouble()); cellStyle = styleA }
                }
                // Col 6: Progressive = Actual (D) + B/F (E)
                r.createCell(5).apply { cellFormula = "D${rowIdxA + 1}+E${rowIdxA + 1}"; cellStyle = styleABold }
                rowIdxA++
            }

            sheetA.setRowBreak(42) // Explicit Page Break at Row 42
            for (i in 0..5) sheetA.autoSizeColumn(i)

            FileOutputStream(fileA).use { wbA.write(it) }
            wbA.close()

            // ==========================================
            // 2. FORMAT B: AG Rec Landscape (Fit 1 Page, 8-Col)
            // ==========================================
            val fileB = File(downloadDir, "Monthly Expenditure Statement $monthYear Landscape.xlsx")
            val wbB = XSSFWorkbook()
            val sheetB = wbB.createSheet("AG Rec")

            val psB = sheetB.printSetup
            psB.paperSize = PrintSetup.LEGAL_PAPERSIZE
            psB.orientation = PrintOrientation.LANDSCAPE
            psB.fitWidth = 1.toShort()
            psB.fitHeight = 1.toShort()
            sheetB.setMargin(Sheet.LeftMargin, 1.10)
            sheetB.setMargin(Sheet.RightMargin, 0.50)
            sheetB.setMargin(Sheet.TopMargin, 0.35)
            sheetB.setMargin(Sheet.BottomMargin, 0.35)

            val fontB = wbB.createFont().apply { fontName = "Book Antiqua"; fontHeightInPoints = 12.toShort(); color = IndexedColors.BLACK.index }
            val fontBBold = wbB.createFont().apply { fontName = "Book Antiqua"; fontHeightInPoints = 12.toShort(); bold = true; color = IndexedColors.BLACK.index }
            val fontBTitle = wbB.createFont().apply { fontName = "Book Antiqua"; fontHeightInPoints = 15.toShort(); bold = true; color = IndexedColors.BLACK.index }
            val fontBCert = wbB.createFont().apply { fontName = "Book Antiqua"; fontHeightInPoints = 11.toShort(); italic = true; color = IndexedColors.BLACK.index }

            val styleB = wbB.createCellStyle().apply { setFont(fontB); borderTop = BorderStyle.THIN; borderBottom = BorderStyle.THIN; borderLeft = BorderStyle.THIN; borderRight = BorderStyle.THIN }
            val styleBBold = wbB.createCellStyle().apply { setFont(fontBBold); borderTop = BorderStyle.THIN; borderBottom = BorderStyle.THIN; borderLeft = BorderStyle.THIN; borderRight = BorderStyle.THIN }
            val styleBTitle = wbB.createCellStyle().apply { setFont(fontBTitle); alignment = HorizontalAlignment.CENTER }
            val styleBCert = wbB.createCellStyle().apply { setFont(fontBCert) }

            val r0B = sheetB.createRow(0)
            r0B.createCell(0).apply { setCellValue("MONTHLY EXPENDITURE STATEMENT & RECONCILIATION WITH AG FIGURES"); cellStyle = styleBTitle }
            val r1B = sheetB.createRow(1)
            r1B.createCell(0).apply { setCellValue("OFFICE OF THE PRESIDING OFFICER, LABOUR COURT, HAZARA REGION AT HARIPUR"); cellStyle = styleBTitle }
            val r2B = sheetB.createRow(2)
            r2B.createCell(0).apply { setCellValue("FOR THE MONTH OF $monthYear (FINANCIAL YEAR 2026-2027) | GRANT NO. 21037 (030) - HR-4007 LABOUR COURTS"); cellStyle = styleBTitle }

            val colHeadersB = listOf(
                "Major / Minor Head", "Budget Estimates 2026-2027", "Revised Budget Estimates 2026-27",
                "Departmental Figures - During Month", "Departmental Figures - Progressive Exp.",
                "A.G Figures - During Month", "A.G Figures - Progressive Exp.", "Variation (Col 5 - Col 7)"
            )
            val hRowB = sheetB.createRow(4)
            colHeadersB.forEachIndexed { i, h ->
                hRowB.createCell(i).apply { setCellValue(h); cellStyle = styleBBold }
            }

            val headsB = listOf(
                "A-01 Total Employees Related Expenses",
                "A-03 Total Operating Expenses",
                "A-04 Total Transfers",
                "A-05 Total Grants",
                "A-09 Total Physical Assets",
                "A-13 Total Repairs and Maintenance",
                "GRAND TOTAL"
            )

            var rowIdxB = 5
            headsB.forEach { head ->
                val r = sheetB.createRow(rowIdxB)
                r.createCell(0).apply { setCellValue(head); cellStyle = styleBBold }
                for (c in 1..6) {
                    r.createCell(c).apply { setCellValue(0.0); cellStyle = styleB }
                }
                // Variation = Col 5 (E) - Col 7 (G)
                r.createCell(7).apply { cellFormula = "E${rowIdxB + 1}-G${rowIdxB + 1}"; cellStyle = styleBBold }
                rowIdxB++
            }

            // Certificates & Signatory
            val certRow1 = sheetB.createRow(rowIdxB + 2)
            certRow1.createCell(0).apply { setCellValue("1. Figures mentioned in the statement are compared with the available posts and sanctioned strength of this office and found correct in all respects."); cellStyle = styleBCert }
            val certRow2 = sheetB.createRow(rowIdxB + 3)
            certRow2.createCell(0).apply { setCellValue("2. It is certified that the payroll register & expenditure figures provided by District Accounts Office Haripur have thoroughly been examined and verified; data of all employees are correct."); cellStyle = styleBCert }

            val sigRow = sheetB.createRow(rowIdxB + 6)
            sigRow.createCell(5).apply { setCellValue("District & Sessions Judge / Presiding Officer\nLabour Court, Hazara Region at Haripur"); cellStyle = styleBBold }

            for (i in 0..7) sheetB.autoSizeColumn(i)
            wbB.setPrintArea(0, 0, 7, 0, rowIdxB + 7)

            FileOutputStream(fileB).use { wbB.write(it) }
            wbB.close()

            // ==========================================
            // 3. FORMAT C: RECONCILED Covering Letter (.docx)
            // ==========================================
            val fileC = File(downloadDir, "RECONCILED Covering Letter $monthYear.docx")
            val doc = XWPFDocument()

            // Dispatch Header
            val dispPara = doc.createParagraph()
            val dispRun = dispPara.createRun()
            dispRun.fontFamily = "Book Antiqua"
            dispRun.fontSize = 12
            dispRun.setText("No. ____________/LCH\t\tDated Haripur the________/________/2026")

            // Addressee
            val addrPara = doc.createParagraph()
            val addrRun = addrPara.createRun()
            addrRun.fontFamily = "Book Antiqua"
            addrRun.fontSize = 12
            addrRun.setText("\nTo,\n        Section Officer (General),\n        Labour Department,\n        Govt; of KP Peshawar.\n\n")

            // Subject Line
            val subPara = doc.createParagraph()
            val subRun = subPara.createRun()
            subRun.fontFamily = "Book Antiqua"
            subRun.fontSize = 12
            subRun.isBold = true
            subRun.underline = org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE
            subRun.setText("SUBJECT:      MONTHLY RECONCILIATION STATEMENT (EXPENDITURE)\n                    FOR THE MONTH OF $monthYear.\n\n")

            // Body Text
            val bodyPara = doc.createParagraph()
            val bodyRun = bodyPara.createRun()
            bodyRun.fontFamily = "Book Antiqua"
            bodyRun.fontSize = 12
            bodyRun.setText("            With reference to subject noted above, enclosed find herewith the monthly reconciliation statement (Expenditure) for the month of $monthYear, duly verified by District Account Officer Haripur for further necessary action, please.\n\n")

            // Enclosure
            val encPara = doc.createParagraph()
            val encRun = encPara.createRun()
            encRun.fontFamily = "Book Antiqua"
            encRun.fontSize = 12
            encRun.isBold = true
            encRun.setText("Encl: as above.\n\n")

            // Signatory 1
            val sig1Para = doc.createParagraph()
            sig1Para.indentFromLeft = 6000
            val sig1Run = sig1Para.createRun()
            sig1Run.fontFamily = "Book Antiqua"
            sig1Run.fontSize = 12
            sig1Run.isBold = true
            sig1Run.setText("D&SJ/Presiding Officer\nLabour Court Haripur\n\n")

            // Endorsement Line
            val endLinePara = doc.createParagraph()
            val endLineRun = endLinePara.createRun()
            endLineRun.fontFamily = "Book Antiqua"
            endLineRun.fontSize = 12
            endLineRun.setText("No. ____________/LCH\t\tDated Haripur the________/________/2026\n")

            // Endorsement Block
            val endPara = doc.createParagraph()
            val endRun = endPara.createRun()
            endRun.fontFamily = "Book Antiqua"
            endRun.fontSize = 12
            endRun.setText("Copy forwarded to: -\n1.  Section Officer (B&A) Labour Department Govt: of KPK Peshawar.\n2.  District Accounts Officer Haripur.\n\n")

            // Endorsement Signatory
            val sig2Para = doc.createParagraph()
            sig2Para.indentFromLeft = 6000
            val sig2Run = sig2Para.createRun()
            sig2Run.fontFamily = "Book Antiqua"
            sig2Run.fontSize = 12
            sig2Run.isBold = true
            sig2Run.setText("D&SJ/Presiding Officer\nLabour Court Haripur")

            FileOutputStream(fileC).use { doc.write(it) }
            doc.close()

            Toast.makeText(
                this,
                "Success! 3 Reconciled Documents saved in Downloads:\n1. Detailed Rec $monthYear Legal Portrait.xlsx\n2. Monthly Expenditure Statement $monthYear Landscape.xlsx\n3. RECONCILED Covering Letter $monthYear.docx",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error generating files: ${e.message}", Toast.LENGTH_LONG).show()
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
                    text = "Monthly Expenditure Statement & Reconciliation",
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
                        label = { Text("Select Financial Month (2026-2027)") },
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
                        text = "Generate All 3 Official Formats",
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
