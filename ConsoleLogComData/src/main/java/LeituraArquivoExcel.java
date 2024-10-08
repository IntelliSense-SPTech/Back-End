public class LeituraArquivoExcel {
    Path caminho = Path.of("melhores-livros.xlsx");
    InputStream arquivo = Files.newInputStream(caminho);

    Workbook workbook = new XSSFWorkbook(arquivo);

    // Acessando a primeira planilha
    Sheet sheet = workbook.getSheetAt(0);

    // Acessando a primeira linha da planilha
    Row row = sheet.getRow(0);

    // Acessando a primeira célula da linha
    Cell cell = row.getCell(0);

    String valor = cell.getStringCellValue();


    workbook.close();


}
