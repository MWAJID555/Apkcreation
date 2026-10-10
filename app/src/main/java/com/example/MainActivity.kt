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
                        onGenerate = { month: String ->
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

            val upperMonth = monthYear.uppercase()

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

            sheetA.createRow(0).createCell(0).apply { setCellValue("MONTHLY EXPENDITURE STATEMENT FOR THE MONTH OF " + upperMonth); cellStyle = styleATitle }
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
                    val colNames = listOf("Object Head", "Budget Estimate\\n2026-2027", "Revised Budget\\nEstimate", "Actual Expenditure\\n$monthYear", "Expenditure\\nB/F", "Progressive\\nExpenditure")
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
                listOf("22", "   A-0120K-Special Judicial Allow 100 percent", "2982000", "-", "280250", "560500", "=D23+E23"),
                listOf("23", "   A-0120K-Special Judicial Allow 50 percent", "-", "-", "-", "-", "=D24+E24"),
                listOf("24", "   A-01217-Medical Allow", "392000", "-", "28446", "56892", "=D25+E25"),
                listOf("25", "   A-0121-T-Adhoc Relief 2013 (15 percent)", "77000", "-", "6264", "12528", "=D26+E26"),
                listOf("26", "   A-01224-Entertanment Allowance", "9000", "-", "700", "1400", "=D27+E27"),
                listOf("27", "   A-0122-C- Adhoc Relief 2015", "54000", "-", "4233", "8466", "=D28+E28"),
                listOf("28", "   A-01239- Special Allowance", "-", "-", "-", "-", "=D29+E29"),
                listOf("29", "   A-01241-Utility Allowance", "416000", "-", "30000", "60000", "=D30+E30"),
                listOf("30", "   A-01248 – Judicial Allowance", "194000", "-", "14000", "28000", "=D31+E31"),
                listOf("31", "   A-0124H – Special Allowance 2021", "278000", "-", "20074", "40148", "=D32+E32"),
                listOf("32", "   A-0124L-Weather Allowance", "11000", "-", "-", "-", "=D33+E33"),
                listOf("33", "   A-0124N- DRA 2022", "585000", "-", "39114", "78228", "=D34+E34"),
                listOf("34", "   A-0124R-Adhoc Relief 2022", "858000", "-", "-", "-", "=D35+E35"),
                listOf("35", "   A-0124X-Adhoc Relief 2023", "2907000", "-", "210349", "420698", "=D36+E36"),
                listOf("36", "   A-0125E-Adhoc Relief 2024", "2077000", "-", "165259", "330518", "=D37+E37"),
                listOf("37", "   A-0125X-Adhoc Relief 2025", "898000", "-", "62032", "124064", "=D38+E38"),
                listOf("38", "   A-0125Q-DRA 2025", "506000", "-", "41463", "82926", "=D39+E39"),
                listOf("39", "   A-012-2 –Total Other Allowance", "-", "-", "-", "-", "=D40+E40"),
                listOf("40", "   A-01274-Medical Charges", "-", "-", "-", "-", "=D41+E41"),
                listOf("41", "   A-01278-Leave Salary", "-", "-", "-", "-", "=D42+E42")
            )

            p1Data.forEach { row ->
                val rIdx = row[0].toInt()
                val head = row[1]
                val isBold = head.contains("TOTAL") || (head.contains("Pay") && !head.contains("of")) || (head.contains("Allowances") && !head.contains("Senior"))
                val textStyle = if (isBold) styleABoldText else styleAText
                val numStyle = if (isBold) styleABoldNum else styleA

                val r = sheetA.createRow(rIdx)
                r.createCell(0).apply { setCellValue(head); cellStyle = textStyle }
                for (c in 1..5) {
                    setSmartCell(r.createCell(c), row[c + 1], numStyle)
                }
            }

            sheetA.setRowBreak(42)
            renderColHeaders(43)

            val p2Data = listOf(
                listOf("45", "A-03-Total Operating Expenses", "=B47+B50+B56+B59+B64", "=C47+C50+C56+C59+C64", "=D47+D50+D56+D59+D64", "=E47+E50+E56+E59+E64", "=F47+F50+F56+F59+F64"),
                listOf("46", "A-032-Communication", "=SUM(B48:B49)", "=SUM(C48:C49)", "=SUM(D48:D49)", "=SUM(E48:E49)", "=SUM(F48:F49)"),
                listOf("47", "   A-03201-Postage & Telegraph", "31000", "-", "-", "-", "=D48+E48"),
                listOf("48", "   A-03202-Telephone Charges", "110000", "-", "9500", "4750", "=D49+E49"),
                listOf("49", "A-033- utilities", "=SUM(B51:B55)", "=SUM(C51:C55)", "=SUM(D51:D55)", "=SUM(E51:E55)", "=SUM(F51:F55)"),
                listOf("50", "   A-03301- Gas Charges", "1000", "-", "-", "-", "=D51+E51"),
                listOf("51", "   A-03302-Water Charges", "-", "-", "-", "-", "=D52+E52"),
                listOf("52", "   A-03303-Electricity Charges", "1000", "-", "-", "-", "=D53+E53"),
                listOf("53", "   A03304-Hot & Cold Charges", "-", "-", "-", "-", "=D54+E54"),
                listOf("54", "   A-03402- Rent of Office Building", "-", "-", "-", "-", "=D55+E55"),
                listOf("55", "A-036-Total Motor Vehicles", "=SUM(B57:B58)", "=SUM(C57:C58)", "=SUM(D57:D58)", "=SUM(E57:E58)", "=SUM(F57:F58)"),
                listOf("56", "   A03603-REGISTRATION", "1000", "-", "-", "-", "=D57+E57"),
                listOf("57", "   A-03670-Others", "-", "-", "-", "-", "=D58+E58"),
                listOf("58", "A-038-Travel and Transportation", "=SUM(B60:B63)", "=SUM(C60:C63)", "=SUM(D60:D63)", "=SUM(E60:E63)", "=SUM(F60:F63)"),
                listOf("59", "   A03801-Training - Domestic", "1000", "-", "-", "-", "=D60+E60"),
                listOf("60", "   A-03805-Travelling Allowance", "1100000", "-", "-", "223000", "=D61+E61"),
                listOf("61", "   A-03807-POL Charges", "1100000", "-", "98141", "112598", "=D62+E62"),
                listOf("62", "   A-03808-Conveyance Charges", "22000", "-", "-", "-", "=D63+E63"),
                listOf("63", "A-039-Total General", "=SUM(B65:B71)", "=SUM(C65:C71)", "=SUM(D65:D71)", "=SUM(E65:E71)", "=SUM(F65:F71)"),
                listOf("64", "   A-03901-Stationery", "182000", "-", "-", "45500", "=D65+E65"),
                listOf("65", "   A-03902-Printing & Publication", "44000", "-", "-", "-", "=D66+E66"),
                listOf("66", "   A-03905-News Paper & Books", "22000", "-", "-", "-", "=D67+E67"),
                listOf("67", "   A-03906-Uniforms & Clothing", "55000", "-", "-", "-", "=D68+E68"),
                listOf("68", "   A-03907-Advertising & Publicity", "1000", "-", "-", "-", "=D69+E69"),
                listOf("69", "   A-03917-Law Charges", "-", "-", "-", "-", "=D70+E70"),
                listOf("70", "   A-03970-Others", "110000", "-", "-", "27500", "=D71+E71"),
                listOf("71", "A04114-Superannuation Encashment of L.P.R", "250000", "-", "-", "-", "=D72+E72"),
                listOf("72", "A05216-Fin Assis. To the Families of G.Serv", "1000", "-", "-", "-", "=D73+E73"),
                listOf("73", "A-09-Total Physical Assets", "=SUM(B75:B77)", "=SUM(C75:C77)", "=SUM(D75:D77)", "=SUM(E75:E77)", "=SUM(F75:F77)"),
                listOf("74", "   A-09201- Hardware /Total Computer Equip", "200000", "-", "-", "-", "=D75+E75"),
                listOf("75", "   A-09601- Total Purchase of Machinery", "500000", "-", "-", "-", "=D76+E76"),
                listOf("76", "   A-09701- Total Purchase of Furniture & Fix", "200000", "-", "-", "-", "=D77+E77"),
                listOf("77", "A-13-Total Repair & maintenance", "=SUM(B79:B82)", "=SUM(C79:C82)", "=SUM(D79:D82)", "=SUM(E79:E82)", "=SUM(F79:F82)"),
                listOf("78", "   A-13001 - Transport", "143000", "-", "35000", "-", "=D79+E79"),
                listOf("79", "   A-13101-Machinery & Equipment", "55000", "-", "-", "-", "=D80+E80"),
                listOf("80", "   A-13201-Furniture & Fixture", "55000", "-", "-", "-", "=D81+E81"),
                listOf("81", "   A-13701- Hardware", "1000", "-", "-", "-", "=D82+E82"),
                listOf("82", "Grand Total", "=B9+B46+B72+B73+B74+B78", "=C9+C46+C72+C73+C74+C78", "=D9+D46+D72+D73+D74+D78", "=E9+E46+E72+E73+E74+E78", "=F9+F46+F72+F73+F74+F78")
            )

            p2Data.forEach { row ->
                val rIdx = row[0].toInt()
                val head = row[1]
                val isBold = head.contains("Total") || head.contains("TOTAL") || head.contains("Grand Total")
                val textStyle = if (isBold) styleABoldText else styleAText
                val numStyle = if (isBold) styleABoldNum else styleA

                val r = sheetA.createRow(rIdx)
                r.createCell(0).apply { setCellValue(head); cellStyle = textStyle }
                for (c in 1..5) {
                    setSmartCell(r.createCell(c), row[c + 1], numStyle)
                }
            }

            sheetA.createRow(86).createCell(3).apply { setCellValue("D&SJ / Presiding Officer"); cellStyle = styleABoldText }
            sheetA.createRow(87).createCell(3).apply { setCellValue("Labour Court Haripur"); cellStyle = styleABoldText }

            sheetA.setColumnWidth(0, 36 * 256)
            for (i in 1..5) {
                sheetA.setColumnWidth(i, 18 * 256)
            }

            FileOutputStream(fileA).use { wbA.write(it) }
            wbA.close()

            // -----------------------------------------------------------------
            // 2. FORMAT B: Monthly Expenditure Statement [Month Year] Landscape.xlsx
            // -----------------------------------------------------------------
            val fileB = File(downloadDir, "Monthly Expenditure Statement $monthYear Landscape.xlsx")
            val wbB = XSSFWorkbook()
            val sheetB = wbB.createSheet("AG Rec")

            val psB = sheetB.printSetup
            psB.paperSize = PrintSetup.LEGAL_PAPERSIZE
            psB.orientation = PrintOrientation.LANDSCAPE
            psB.fitWidth = 1.toShort()
            psB.fitHeight = 1.toShort()
            sheetB.autobreaks = false
            sheetB.setMargin(Sheet.LeftMargin, 1.10)
            sheetB.setMargin(Sheet.RightMargin, 0.50)
            sheetB.setMargin(Sheet.TopMargin, 0.35)
            sheetB.setMargin(Sheet.BottomMargin, 0.35)

            val fontB = wbB.createFont().apply { fontName = "Book Antiqua"; fontHeightInPoints = 12.toShort(); color = IndexedColors.BLACK.index }
            val fontBBold = wbB.createFont().apply { fontName = "Book Antiqua"; fontHeightInPoints = 12.toShort(); bold = true; color = IndexedColors.BLACK.index }
            val fontBCert = wbB.createFont().apply { fontName = "Book Antiqua"; fontHeightInPoints = 11.toShort(); italic = true; color = IndexedColors.BLACK.index }

            val styleB = wbB.createCellStyle().apply { setFont(fontB); borderTop = borderStyle; borderBottom = borderStyle; borderLeft = borderStyle; borderRight = borderStyle; alignment = HorizontalAlignment.RIGHT }
            val styleBText = wbB.createCellStyle().apply { setFont(fontB); borderTop = borderStyle; borderBottom = borderStyle; borderLeft = borderStyle; borderRight = borderStyle; alignment = HorizontalAlignment.LEFT }
            val styleBBold = wbB.createCellStyle().apply { setFont(fontBBold); borderTop = borderStyle; borderBottom = borderStyle; borderLeft = borderStyle; borderRight = borderStyle; alignment = HorizontalAlignment.RIGHT }
            val styleBBoldText = wbB.createCellStyle().apply { setFont(fontBBold); borderTop = borderStyle; borderBottom = borderStyle; borderLeft = borderStyle; borderRight = borderStyle; alignment = HorizontalAlignment.LEFT }
            val styleBHeader = wbB.createCellStyle().apply { setFont(fontBBold); borderTop = borderStyle; borderBottom = borderStyle; borderLeft = borderStyle; borderRight = borderStyle; alignment = HorizontalAlignment.CENTER; verticalAlignment = VerticalAlignment.CENTER; wrapText = true }

            sheetB.createRow(0).createCell(0).apply { setCellValue("MONTHLY EXPENDITURE STATEMENT & RECONCILIATION WITH AG FIGURES"); cellStyle = wbB.createCellStyle().apply { setFont(wbB.createFont().apply { fontName = "Book Antiqua"; fontHeightInPoints = 15.toShort(); bold = true; color = IndexedColors.BLACK.index }); alignment = HorizontalAlignment.CENTER } }
            sheetB.createRow(1).createCell(0).apply { setCellValue("OFFICE OF THE PRESIDING OFFICER, LABOUR COURT, HAZARA REGION AT HARIPUR"); cellStyle = wbB.createCellStyle().apply { setFont(wbB.createFont().apply { fontName = "Book Antiqua"; fontHeightInPoints = 13.toShort(); bold = true; color = IndexedColors.BLACK.index }); alignment = HorizontalAlignment.CENTER } }
            sheetB.createRow(2).createCell(0).apply { setCellValue("FOR THE MONTH OF " + upperMonth + " (FINANCIAL YEAR 2026-2027) | GRANT NO. 21037 (030) - HR-4007 LABOUR COURTS"); cellStyle = wbB.createCellStyle().apply { setFont(wbB.createFont().apply { fontName = "Book Antiqua"; fontHeightInPoints = 11.toShort(); bold = true; color = IndexedColors.BLACK.index }); alignment = HorizontalAlignment.CENTER } }
            for (r in 0..2) sheetB.addMergedRegion(CellRangeAddress(r, r, 0, 7))

            val r3B = sheetB.createRow(3)
            r3B.createCell(0).apply { setCellValue("Major / Minor Head"); cellStyle = styleBHeader }
            r3B.createCell(1).apply { setCellValue("Budget Estimates\\nfor 2026-2027"); cellStyle = styleBHeader }
            r3B.createCell(2).apply { setCellValue("Revised Budget\\nEstimates 2026-27"); cellStyle = styleBHeader }
            r3B.createCell(3).apply { setCellValue("Departmental Figures"); cellStyle = styleBHeader }
            r3B.createCell(4).apply { cellStyle = styleBHeader }
            r3B.createCell(5).apply { setCellValue("A.G Figures"); cellStyle = styleBHeader }
            r3B.createCell(6).apply { cellStyle = styleBHeader }
            r3B.createCell(7).apply { setCellValue("Variation\\n(Col 5 - Col 7)"); cellStyle = styleBHeader }

            sheetB.addMergedRegion(CellRangeAddress(3, 4, 0, 0))
            sheetB.addMergedRegion(CellRangeAddress(3, 4, 1, 1))
            sheetB.addMergedRegion(CellRangeAddress(3, 4, 2, 2))
            sheetB.addMergedRegion(CellRangeAddress(3, 3, 3, 4))
            sheetB.addMergedRegion(CellRangeAddress(3, 3, 5, 6))
            sheetB.addMergedRegion(CellRangeAddress(3, 4, 7, 7))

            val r4B = sheetB.createRow(4)
            r4B.createCell(3).apply { setCellValue("During Month (" + monthYear.take(3) + ")"); cellStyle = styleBHeader }
            r4B.createCell(4).apply { setCellValue("Progressive Exp."); cellStyle = styleBHeader }
            r4B.createCell(5).apply { setCellValue("During Month (" + monthYear.take(3) + ")"); cellStyle = styleBHeader }
            r4B.createCell(6).apply { setCellValue("Progressive Exp."); cellStyle = styleBHeader }

            sheetB.createRow(5).apply {
                for (c in 0..7) createCell(c).apply { setCellValue((c + 1).toString()); cellStyle = styleBHeader }
            }

            val bData = listOf(
                listOf("6", "A-011-Total Pay", "10479000", "-", "886210", "=D7+1772420", "886210", "=F7+1772420", "=E7-G7"),
                listOf("7", "A-012-01-Total Allowance", "14072000", "-", "1041357", "=D8+2082714", "1041357", "=F8+2082714", "=E8-G8"),
                listOf("8", "Total (A-01 Employees Related Expenses)", "=SUM(B7:B8)", "=SUM(C7:C8)", "=SUM(D7:D8)", "=SUM(E7:E8)", "=SUM(F7:F8)", "=SUM(G7:G8)", "=E9-G9"),
                listOf("9", "A-032-Communication", "141000", "-", "9500", "=D10+4750", "9500", "=F10+4750", "=E10-G10"),
                listOf("10", "A-033-Utilities", "2000", "-", "-", "=D11", "-", "=F11", "=E11-G11"),
                listOf("11", "A-036-Motor Vehicles (Registration)", "1000", "-", "-", "=D12", "-", "=F12", "=E12-G12"),
                listOf("12", "A-038-Travel & Transportation (TA / POL)", "2223000", "-", "98141", "=D13+335598", "98141", "=F13+335598", "=E13-G13"),
                listOf("13", "A-039-General (Stationery, Printing, Others)", "414000", "-", "-", "=D14+73000", "-", "=F14+73000", "=E14-G14"),
                listOf("14", "A-04-Superannuation Encashment of L.P.R", "250000", "-", "-", "=D15", "-", "=F15", "=E15-G15"),
                listOf("15", "A-05-Financial Assistance to Families of G.Serv.", "1000", "-", "-", "=D16", "-", "=F16", "=E16-G16"),
                listOf("16", "Total Operating Expenses & Transfers (A-03, A-04, A-05)", "=SUM(B10:B16)", "=SUM(C10:C16)", "=SUM(D10:D16)", "=SUM(E10:E16)", "=SUM(F10:F16)", "=SUM(G10:G16)", "=E17-G17"),
                listOf("17", "   A-09201-Hardware / Computer Equipment", "200000", "-", "-", "=D18", "-", "=F18", "=E18-G18"),
                listOf("18", "   A-09601-Purchase of Plant & Machinery", "500000", "-", "-", "=D19", "-", "=F19", "=E19-G19"),
                listOf("19", "   A-09701-Purchase of Furniture & Fixture", "200000", "-", "-", "=D20", "-", "=F20", "=E20-G20"),
                listOf("20", "Total Physical Assets (A-09)", "=SUM(B18:B20)", "=SUM(C18:C20)", "=SUM(D18:D20)", "=SUM(E18:E20)", "=SUM(F18:F20)", "=SUM(G18:G20)", "=E21-G21"),
                listOf("21", "   A-13001-Transport", "143000", "-", "35000", "=D22", "35000", "=F22", "=E22-G22"),
                listOf("22", "   A-13101-Machinery & Equipment", "55000", "-", "-", "=D23", "-", "=F23", "=E23-G23"),
                listOf("23", "   A-13201-Furniture & Fixture", "55000", "-", "-", "=D24", "-", "=F24", "=E24-G24"),
                listOf("24", "   A-13701-Hardware", "1000", "-", "-", "=D25", "-", "=F25", "=E25-G25"),
                listOf("25", "Total Repair & Maintenance (A-13)", "=SUM(B22:B25)", "=SUM(C22:C25)", "=SUM(D22:D25)", "=SUM(E22:E25)", "=SUM(F22:F25)", "=SUM(G22:G25)", "=E26-G26"),
                listOf("26", "Grand Total", "=B9+B17+B21+B26", "=C9+C17+C21+C26", "=D9+D17+D21+D26", "=E9+E17+E21+E26", "=F9+F17+F21+F26", "=G9+G17+G21+G26", "=E27-G27")
            )

            bData.forEach { row ->
                val rIdx = row[0].toInt()
                val head = row[1]
                val isBold = head.contains("Total") || head.contains("TOTAL") || head.contains("Grand Total")
                val textStyle = if (isBold) styleBBoldText else styleBText
                val numStyle = if (isBold) styleBBold else styleB

                val r = sheetB.createRow(rIdx)
                r.createCell(0).apply { setCellValue(head); cellStyle = textStyle }
                for (c in 1..7) {
                    setSmartCell(r.createCell(c), row[c + 1], numStyle)
                }
            }

            val certStyle = wbB.createCellStyle().apply { setFont(fontBCert); wrapText = true }
            val cRow1 = sheetB.createRow(28)
            cRow1.createCell(0).apply { setCellValue("CERTIFICATE:\\n\\"Figures mentioned in the statement are compared with available posts and sanctioned strength of this office and found correct in all respects.\\""); cellStyle = certStyle }
            cRow1.createCell(4).apply { setCellValue("CERTIFICATE:\\n\\"It is certified that payroll register & expenditure figures provided by District Accounts Office Haripur have thoroughly been examined & verified; data of all employees are correct.\\""); cellStyle = certStyle }
            sheetB.addMergedRegion(CellRangeAddress(28, 30, 0, 3))
            sheetB.addMergedRegion(CellRangeAddress(28, 30, 4, 7))

            sheetB.createRow(32).createCell(4).apply { setCellValue("__________________________________________________"); cellStyle = styleBText }
            sheetB.createRow(33).createCell(4).apply { setCellValue("District & Sessions Judge / Presiding Officer"); cellStyle = styleBBoldText }
            sheetB.createRow(34).createCell(4).apply { setCellValue("Labour Court, Hazara Region at Haripur"); cellStyle = styleBBoldText }

            sheetB.setColumnWidth(0, 32 * 256)
            for (i in 1..7) {
                sheetB.setColumnWidth(i, 16 * 256)
            }
            wbB.setPrintArea(0, 0, 7, 0, 34)

            FileOutputStream(fileB).use { wbB.write(it) }
            wbB.close()

            // -----------------------------------------------------------------
            // 3. FORMAT C: RECONCILED Covering Letter [Month Year].docx
            // -----------------------------------------------------------------
            val fileC = File(downloadDir, "RECONCILED Covering Letter $monthYear.docx")
            val doc = XWPFDocument()

            val topPara = doc.createParagraph()
            topPara.alignment = ParagraphAlignment.RIGHT
            val topRun = topPara.createRun()
            topRun.fontFamily = "Book Antiqua"
            topRun.fontSize = 10
            topRun.setText("Off No. 0995-613125\\nE-mail: hrp4007@gmail.com")

            val titleP = doc.createParagraph()
            titleP.alignment = ParagraphAlignment.CENTER
            val titleR = titleP.createRun()
            titleR.fontFamily = "Book Antiqua"
            titleR.fontSize = 14
            titleR.isBold = true
            titleR.setText("LABOUR COURT\\nHazara Region at Haripur\\n\\n")

            val dispP = doc.createParagraph()
            val dispR = dispP.createRun()
            dispR.fontFamily = "Book Antiqua"
            dispR.fontSize = 12
            dispR.setText("No. ____________/LCH\\t\\tDated Haripur the________/________/2026\\n")

            val addrP = doc.createParagraph()
            val addrR = addrP.createRun()
            addrR.fontFamily = "Book Antiqua"
            addrR.fontSize = 12
            addrR.setText("To,\\n        Section Officer (General),\\n        Labour Department,\\n        Govt; of KP Peshawar.\\n\\n")

            val subP = doc.createParagraph()
            val subR = subP.createRun()
            subR.fontFamily = "Book Antiqua"
            subR.fontSize = 12
            subR.isBold = true
            subR.underline = UnderlinePatterns.SINGLE
            subR.setText("SUBJECT:      MONTHLY RECONCILIATION STATEMENT (EXPENDITURE)\\n                    FOR THE MONTH OF " + upperMonth + ".\\n\\n")

            val bodyP = doc.createParagraph()
            val bodyR = bodyP.createRun()
            bodyR.fontFamily = "Book Antiqua"
            bodyR.fontSize = 12
            bodyR.setText("            With reference to subject noted above, enclosed find herewith the monthly reconciliation statement (Expenditure) for the month of $monthYear, duly verified by District Account Officer Haripur for further necessary action, please.\\n\\n")

            val encP = doc.createParagraph()
            val encR = encP.createRun()
            encR.fontFamily = "Book Antiqua"
            encR.fontSize = 12
            encR.isBold = true
            encR.setText("Encl: as above.\\n\\n")

            val sig1P = doc.createParagraph()
            sig1P.indentFromLeft = 6000
            val sig1R = sig1P.createRun()
            sig1R.fontFamily = "Book Antiqua"
            sig1R.fontSize = 12
            sig1R.isBold = true
            sig1R.setText("D&SJ/Presiding Officer\\nLabour Court Haripur\\n\\n")

            val endLP = doc.createParagraph()
            val endLR = endLP.createRun()
            endLR.fontFamily = "Book Antiqua"
            endLR.fontSize = 12
            endLR.setText("No. ____________/LCH\\t\\tDated Haripur the________/________/2026\\n")

            val endP = doc.createParagraph()
            val endR = endP.createRun()
            endR.fontFamily = "Book Antiqua"
            endR.fontSize = 12
            endR.setText("Copy forwarded to: -\\n1.  Section Officer (B&A) Labour Department Govt: of KPK Peshawar.\\n2.  District Accounts Officer Haripur.\\n\\n")

            val sig2P = doc.createParagraph()
            sig2P.indentFromLeft = 6000
            val sig2R = sig2P.createRun()
            sig2R.fontFamily = "Book Antiqua"
            sig2R.fontSize = 12
            sig2R.isBold = true
            sig2R.setText("D&SJ/Presiding Officer\\nLabour Court Haripur")

            FileOutputStream(fileC).use { doc.write(it) }
            doc.close()

            Toast.makeText(
                this,
                "Success! 3 Official Documents Generated:\\n1. Detailed Rec $monthYear Legal Portrait.xlsx\\n2. Monthly Expenditure Statement $monthYear Landscape.xlsx\\n3. RECONCILED Covering Letter $monthYear.docx",
                Toast.LENGTH_LONG
            ).show()

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
                        statusText = "Generating official files for $selectedMonth..."
                        onGenerate(selectedMonth)
                        statusText = "Generated All 3 Files for $selectedMonth!"
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
