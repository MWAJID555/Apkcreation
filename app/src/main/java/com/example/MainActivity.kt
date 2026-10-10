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
import org.apache.poi.xwpf.usermodel.UnderlinePatterns
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

    private fun setSmartCell(cell: org.apache.poi.ss.usermodel.Cell, rawVal: String, style: CellStyle) {
        cell.cellStyle = style
        val v = rawVal.trim()
        if (v.startsWith("=")) {
            cell.cellFormula = v.substring(1)
        } else {
            val num = v.toDoubleOrNull()
            if (num != null) {
                cell.setCellValue(num)
            } else {
                cell.setCellValue(v)
            }
        }
    }

    private fun generateReconciliationFiles(monthYear: String) {
        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) downloadDir.mkdirs()

            // -----------------------------------------------------------------
            // 1. FORMAT A: Detailed Rec [Month Year] Legal Portrait.xlsx
            // -----------------------------------------------------------------
            val fileA = File(downloadDir, "Detailed Rec $monthYear Legal Portrait.xlsx")
            val wbA = XSSFWorkbook()
            val sheetA = wbA.createSheet("Detailed Rec")

            val psA = sheetA.printSetup
            psA.paperSize = PrintSetup.LEGAL_PAPERSIZE
            psA.orientation = PrintOrientation.PORTRAIT
            psA.fitWidth = 1.toShort()
            psA.fitHeight = 2.toShort()
            sheetA.autobreaks = false
            sheetA.setMargin(Sheet.LeftMargin, 0.70)
            sheetA.setMargin(Sheet.RightMargin, 0.50)
            sheetA.setMargin(Sheet.TopMargin, 0.50)
            sheetA.setMargin(Sheet.BottomMargin, 0.50)

            val fontA = wbA.createFont().apply { fontName = "Book Antiqua"; fontHeightInPoints = 12.toShort(); color = IndexedColors.BLACK.index }
            val fontABold = wbA.createFont().apply { fontName = "Book Antiqua"; fontHeightInPoints = 12.toShort(); bold = true; color = IndexedColors.BLACK.index }

            val borderStyle = BorderStyle.THIN
            val styleA = wbA.createCellStyle().apply { setFont(fontA); borderTop = borderStyle; borderBottom = borderStyle; borderLeft = borderStyle; borderRight = borderStyle; alignment = HorizontalAlignment.RIGHT }
            val styleAText = wbA.createCellStyle().apply { setFont(fontA); borderTop = borderStyle; borderBottom = borderStyle; borderLeft = borderStyle; borderRight = borderStyle; alignment = HorizontalAlignment.LEFT }
            val styleABoldNum = wbA.createCellStyle().apply { setFont(fontABold); borderTop = borderStyle; borderBottom = borderStyle; borderLeft = borderStyle; borderRight = borderStyle; alignment = HorizontalAlignment.RIGHT }
            val styleABoldText = wbA.createCellStyle().apply { setFont(fontABold); borderTop = borderStyle; borderBottom = borderStyle; borderLeft = borderStyle; borderRight = borderStyle; alignment = HorizontalAlignment.LEFT }
            val styleAHeader = wbA.createCellStyle().apply { setFont(fontABold); borderTop = borderStyle; borderBottom = borderStyle; borderLeft = borderStyle; borderRight = borderStyle; alignment = HorizontalAlignment.CENTER; verticalAlignment = VerticalAlignment.CENTER; wrapText = true }
            val styleATitle = wbA.createCellStyle().apply { setFont(fontABold); alignment = HorizontalAlignment.CENTER }

            sheetA.createRow(0).createCell(0).apply { setCellValue("MONTHLY EXPENDITURE STATEMENT FOR THE MONTH OF " + monthYear.uppercase()); cellStyle = styleATitle }
            sheetA.createRow(1).createCell(0).apply { setCellValue("OF THE OFFICE OF PRESIDING OFFICER LABOUR COURT HARIPUR"); cellStyle = styleATitle }
            sheetA.createRow(2).createCell(0).apply { setCellValue("GRANT NO. 21037 (030)"); cellStyle = styleATitle }
            sheetA.createRow(3).createCell(0).apply { setCellValue("UNDER HEADS 03- PUBLIC ORDER & SAFETY AFFAIRS, 031-LAW"); cellStyle = styleATitle }
            sheetA.createRow(4).createCell(0).apply { setCellValue("COURTS 031101 COURTS/ JUSTICE HR-4007– LABOUR COURTS"); cellStyle = styleATitle }
            for (r in 0..4) sheetA.addMergedRegion(CellRangeAddress(r, r, 0, 5))

            fun renderColHeaders(startRow: Int) {
                sheetA.createRow(startRow).apply {
                    for (c in 0..5) createCell(c).apply { setCellValue((c + 1).toString()); cellStyle = styleAHeader }
                }
                sheetA.createRow(startRow + 1).apply {
                    heightInPoints = 32f
                    val colNames = listOf("Object Head", "Budget Estimate\n2026-2027", "Revised Budget\nEstimate", "Actual Expenditure\n$monthYear", "Expenditure\nB/F", "Progressive\nExpenditure")
                    colNames.forEachIndexed { i, h -> createCell(i).apply { setCellValue(h); cellStyle = styleAHeader } }
                }
            }

            renderColHeaders(6)

            val p1Data = listOf(
                listOf("8", "A-01-Employees Related Expenses", "=B10+B14", "=C10+C14", "=D10+D14", "=E10+E14", "=F10+F14"),
                listOf("9", "A-011-Pay", "=SUM(B11:B13)", "=SUM(C11:C13)", "=SUM(D11:D13)", "=SUM(E11:E13)", "=SUM(F11:F13)"),
                listOf("10", "   A-01101- Pay of Officer", "5399000", "-", "457370", "914740", "=D11+E11"),
                listOf("11", "   A-01102- Personal Pay", "46000", "-", "71360", "142720", "=D12+E12"),
                listOf("12", "   A01151 – Pay of Staff", "5034000", "-", "357480", "714960", "=D13+E13"),
                listOf("13", "A012-Allowances", "=B15", "=C15", "=D15", "=E15", "=F15"),
                listOf("14", "A-01201-Regular Allowances", "=SUM(B16:B42)", "=SUM(C16:C42)", "=SUM(D16:D42)", "=SUM(E16:E42)", "=SUM(F16:F42)"),
                listOf("15", "   A-01201-Senior Post Allowance", "19000", "-", "1350", "2700", "=D16+E16"),
                listOf("16", "   A-01202-House Rent Allowance", "859000", "-", "58357", "116714", "=D17+E17"),
                listOf("17", "   A-01203-Conveyance Allowance", "555000", "-", "53466", "106932", "=D18+E18"),
                listOf("18", "   A-01207-Washing Allowance", "152000", "-", "10000", "20000", "=D19+E19"),
                listOf("19", "   A-01208-Dress Allowance", "152000", "-", "10000", "20000", "=D20+E20"),
                listOf("20", "   A-0120D- Integrated Allowance", "91000", "-", "6000", "12000", "=D21+E21"),
                listOf("21", "   A-01266- Transfer & Dist Allowance", "-", "-", "-", "-", "=D22+E22"),
                listOf("22", "   A-0120K-Special Judicial Allow 100 %", "2982000", "-", "280250", "560500", "=D2
