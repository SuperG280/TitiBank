package com.superg280.dev.titibank;

import java.io.Serializable;
import java.util.UUID;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.util.Locale;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;


@IgnoreExtraProperties
public class TitiItem implements Serializable {

    public final static int ITEM_TYPE_NOME  = 0;
    public final static int ITEM_TYPE_PRESTAMO = 1;
    public final static int ITEM_TYPE_INGRESO = 2;

    private String id;
    private long fecha;
    private String descripcion;
    private int itemType;
    private long importe;
    private String nota;

    public void regenerateID() {
        this.id = UUID.randomUUID().toString();
    }

    public TitiItem() {
        regenerateID();
        this.importe        = 0;
        this.fecha          = Calendar.getInstance().getTimeInMillis();
        this.descripcion    = "";
        this.nota           = "";
        this.itemType       = ITEM_TYPE_NOME;
    }

    public TitiItem( long fecha, String descripcion, long importe, int itemType) {
        regenerateID() ;
        this.fecha          = fecha;
        this.descripcion    = descripcion;
        this.importe        = importe;
        this.nota           = "";
        this.itemType       = itemType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public long getImporte() {
        return importe;
    }

    public void setImporte(long importe) {
        this.importe = importe;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public String formatFecha() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis( fecha);
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
        return df.format(cal.getTime());
    }

    @Exclude
    public String formatImporte() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return  nf.format( (double)importe / 100);

    }

    public Calendar fechaToCalendar( ) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis( this.fecha);
        return cal;
    }

    public boolean hasNota() {
        return !this.nota.isEmpty();
    }
}
