package itsincom.webdev2425.persistence.repository;

import com.mongodb.client.MongoCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;
import itsincom.webdev2425.persistence.model.DettaglioProdotto;
import itsincom.webdev2425.persistence.model.Ordine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class OrdineRepository implements PanacheMongoRepository<Ordine> {
    public List<Ordine> getOrdini() {
        // dal più recente al più vecchio
        return listAll(Sort.by("data").descending());
    }

    public Ordine getById(String id) {
        ObjectId objectId = new ObjectId(id);
        return findById(objectId);
    }

    public Ordine addOrdine(String email_utente, List<DettaglioProdotto> dettaglio, LocalDateTime data_ritiro, String commento) {
        List<Ordine> ordini = getOrdini();
        if (data_ritiro.isBefore(LocalDateTime.now())) { // Non è possibile effettuare ordini nel passato
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Non è possibile effettuare ordini nel passato").build());
        } else {
            boolean isClosed = isClosed(data_ritiro);
            if (isClosed) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Non è possibile effettuare ordini fuori dall'orario di apertura").build());
            } else {
                for (Ordine o : ordini) {
                    if (Math.abs(o.getData_ritiro().until(data_ritiro, ChronoUnit.MINUTES)) < 10) { // 10 minuti di attesa tra un ordine e l'altro
                        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Devi attendere almeno 10 minuti tra un ordine e l'altro").build());
                    }
                }
            }
        }
        Ordine ordine = Ordine.create(email_utente, dettaglio, data_ritiro, commento);
        persist(ordine);
        return ordine;
    }

    private boolean isClosed(LocalDateTime data_ritiro) {
        int dayOfWeek = data_ritiro.getDayOfWeek().getValue();
        int hour = data_ritiro.getHour();
        int minute = data_ritiro.getMinute();

        boolean isClosed = (dayOfWeek == 1) || // Lunedì chiuso
                           (dayOfWeek == 7 && (hour < 8 || (hour == 12 && minute > 30) || hour > 12)) || // Domenica 8:00-12:30
                           (dayOfWeek >= 2 && dayOfWeek <= 6 && // Martedì-Sabato 7:30-13:00 e 14:30-17:00
                            ((hour < 7 || (hour == 7 && minute < 30)) || (hour == 13 && minute > 0) || (hour > 13 && hour < 14) || (hour == 14 && minute < 30) || hour > 17 || (hour == 17 && minute > 0)));
        return isClosed;
    }

    public List<Ordine> getStoricoOrdiniUtente(String email_utente) {
        List<Ordine> ordini = new ArrayList<>();
        MongoCollection<Document> collection = this.mongoDatabase().getCollection("ordine");
        collection.aggregate(
                List.of(
                        new Document("$match",
                                new Document("email_utente", email_utente)
                                        .append("stato",
                                                new Document("$in", List.of("RITIRATO")))),
                        new Document("$sort",
                                new Document("data_ritiro", -1L))
                )
        ).forEach(document -> {
            addOrderFromDocument(document, ordini);
        });
        return ordini;
    }

    public List<Ordine> getOrdiniCorrentiUtente(String email_utente) {
        List<Ordine> ordini = new ArrayList<>();
        MongoCollection<Document> collection = this.mongoDatabase().getCollection("ordine");
        collection.aggregate(
                List.of(
                        new Document("$match",
                                new Document("email_utente", email_utente)
                                        .append("stato",
                                                new Document("$in", List.of("IN ATTESA DI CONFERMA", "IN PREPARAZIONE")))),
                        new Document("$sort",
                                new Document("data_ritiro", -1L))
                )
        ).forEach(document -> {
            addOrderFromDocument(document, ordini);
        });
        return ordini;
    }

    public Ordine update(Ordine ordine, String id) {
        Ordine ordineDaAggiornare = getById(id);
        ordineDaAggiornare.setStato(ordine.getStato());
        update(ordineDaAggiornare);
        return ordineDaAggiornare;
    }

    public void getExcelOrdini(String data) {
        LocalDate dataRitiro = LocalDate.parse(data);
        LocalDateTime startOfDay = dataRitiro.atStartOfDay();
        LocalDateTime endOfDay = dataRitiro.plusDays(1).atStartOfDay();
        List<Ordine> ordini = list("data_ritiro >= ?1 and data_ritiro < ?2", startOfDay, endOfDay);
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Ordini");

        // Creazione degli stili
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderTop(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);

        CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);

        CellStyle inPreparazioneStyle = workbook.createCellStyle();
        inPreparazioneStyle.cloneStyleFrom(borderStyle);
        inPreparazioneStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        inPreparazioneStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle inAttesaDiConfermaStyle = workbook.createCellStyle();
        inAttesaDiConfermaStyle.cloneStyleFrom(borderStyle);
        inAttesaDiConfermaStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        inAttesaDiConfermaStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle ritiratoStyle = workbook.createCellStyle();
        ritiratoStyle.cloneStyleFrom(borderStyle);
        ritiratoStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        ritiratoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Creazione dell'intestazione
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Email utente", "Data Creazione Ordine", "Data Rititro", "Ora Ritiro", "Stato", "Dettaglio", "Prezzo Totale"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Ordine o : ordini) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(o.getEmail_utente());
            cell.setCellStyle(borderStyle);

            cell = row.createCell(1);
            cell.setCellValue(o.getData().toString());
            cell.setCellStyle(borderStyle);

            cell = row.createCell(2);
            cell.setCellValue(o.getData_ritiro().toLocalDate().toString());
            cell.setCellStyle(borderStyle);

            cell = row.createCell(3);
            cell.setCellValue(o.getData_ritiro().toLocalTime().toString());
            cell.setCellStyle(borderStyle);

            cell = row.createCell(4);
            cell.setCellValue(o.getStato());
            switch (o.getStato()) {
                case "IN PREPARAZIONE":
                    cell.setCellStyle(inPreparazioneStyle);
                    break;
                case "IN ATTESA DI CONFERMA":
                    cell.setCellStyle(inAttesaDiConfermaStyle);
                    break;
                case "RITIRATO":
                    cell.setCellStyle(ritiratoStyle);
                    break;
                default:
                    cell.setCellStyle(borderStyle);
                    break;
            }

            cell = row.createCell(5);
            StringBuilder dettaglio = new StringBuilder();
            for (DettaglioProdotto d : o.getDettaglio()) {
                dettaglio.append(d.getNome()).append(" x").append(d.getQuantita()).append(", ");
            }
            cell.setCellValue(dettaglio.toString());
            cell.setCellStyle(borderStyle);

            cell = row.createCell(6);
            cell.setCellValue(o.getPrezzoTotale());
            cell.setCellStyle(borderStyle);
        }

        // Scrittura del file
        try (FileOutputStream fileOut = new FileOutputStream(System.getProperty("user.home") + "/Downloads/ordini-" + data + ".xls")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // private methods
    private void addOrderFromDocument(Document document, List<Ordine> ordini) {
        Ordine ordine = new Ordine();
        ordine.setId(document.getObjectId("_id"));
        ordine.setEmail_utente(document.getString("email_utente"));
        ordine.setData(document.getDate("data").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        ordine.setData_ritiro(document.getDate("data_ritiro").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        ordine.setStato(document.getString("stato"));
        ordine.setDettaglio(document.getList("dettaglio", Document.class).stream().map(d -> {
            DettaglioProdotto dettaglioProdotto = new DettaglioProdotto();
            dettaglioProdotto.setNome(d.getString("nome"));
            dettaglioProdotto.setQuantita(d.getInteger("quantita"));
            return dettaglioProdotto;
        }).toList());
        ordine.setPrezzoTotale(document.getDouble("prezzoTotale"));
        ordini.add(ordine);
    }
}
