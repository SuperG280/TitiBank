package com.superg280.dev.titibank;


import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ArrayList<TitiItem> titiItems = null;
    private ItemTableAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewItem();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();


        titiItems = new ArrayList<>();
        refilMokItems();

        adapter = new ItemTableAdapter( this, titiItems);
        ListView lv = (ListView) findViewById(R.id.listView_Items);
        lv.setAdapter(adapter);

        paintResumen();
        paintChartTotal();
    }

    public String formatImporte( double importe) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return  nf.format(importe);
    }

    public void paintResumen() {

        TextView txTotalPrestado = findViewById( R.id.textView_main_total_prestado);
        TextView txTotalIngresado = findViewById( R.id.textView_main_total_devuelto);
        TextView txTotalPendiente = findViewById( R.id.textView_main_total_pendiente);

        long totalPrestado = getTotalPrestado();
        txTotalPrestado.setText( formatImporte( (double) totalPrestado / (double) 100));

        long totalIngresado = getTotalIngresado();
        txTotalIngresado.setText( formatImporte( (double) totalIngresado / (double) 100));

        long totalPendiente = totalPrestado - totalIngresado;
        txTotalPendiente.setText( formatImporte( (double) totalPendiente / (double) 100));
    }

    public long getTotal( int type) {

        long total = 0;
        for( TitiItem item : titiItems) {
            if( item.getItemType() == type) {
                total += item.getImporte();
            }
        }
        return total;
    }

    public long getTotalPrestado() {
        return getTotal( TitiItem.ITEM_TYPE_PRESTAMO);
    }

    public long getTotalIngresado() {
        return getTotal( TitiItem.ITEM_TYPE_INGRESO);
    }

    public void paintChartTotal() {
        PieChart pieChart = findViewById( R.id.piechart_porcentaje);

        pieChart.setUsePercentValues(true);

        List<PieEntry> yvalues = new ArrayList<>();
        long totalPrestamo = getTotalPrestado();
        long totalIngresado = getTotalIngresado();
        yvalues.add(new PieEntry( getPorcentaje( totalPrestamo, totalIngresado), "%"));
        yvalues.add(new PieEntry( getPorcentaje( totalPrestamo, totalPrestamo - totalIngresado), "%"));
        PieDataSet dataSet = new PieDataSet(yvalues, "");

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getColor(R.color.colorAccent));
        colors.add(getColor(R.color.colorPendiente));

        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.RED);

        PieData data = new PieData( dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(18f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData( data);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setTransparentCircleRadius(58f);
        pieChart.setHoleRadius(38f);

        pieChart.setDescription( null);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor( Color.BLACK);
        pieChart.animateXY(1400, 1400);
        pieChart.invalidate();
    }

    public float getPorcentaje( long total, long gasto) {

        if( total == 0 || gasto == 0)
            return 0;

        float fgasto = (float)gasto / (float)100;
        float ftotal = (float)total / (float)100;

        return (fgasto * (float)100)/  ftotal;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDatePickerDialog(final EditText editTextFecha) {

        //Calendario para obtener fecha & hora
        Calendar c = Calendar.getInstance();

        //Variables para obtener la fecha
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);
        int anio = c.get(Calendar.YEAR);

        DatePickerDialog recogerFecha = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                //Esta variable lo que realiza es aumentar en uno el mes ya que comienza desde 0 = enero
                final int mesActual = month + 1;
                //Formateo el día obtenido: antepone el 0 si son menores de 10
                String diaFormateado = (dayOfMonth < 10)? 0 + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                //Formateo el mes obtenido: antepone el 0 si son menores de 10
                String mesFormateado = (mesActual < 10)? 0 + String.valueOf(mesActual):String.valueOf(mesActual);
                //Muestro la fecha con el formato deseado
                editTextFecha.setText(diaFormateado + "/" + mesFormateado + "/" + year);
            }
            //Estos valores deben ir en ese orden, de lo contrario no mostrara la fecha actual
            /**
             *También puede cargar los valores que usted desee
             */
        },anio, mes, dia);
        //Muestro el widget
        recogerFecha.show();
    }

    public void createNewItem() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        View v = inflater.inflate(R.layout.new_item_layout, null);

        final EditText editTextFecha = v.findViewById(R.id.editText_new_item_fecha);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        editTextFecha.setText( df.format(cal.getTime()));

        editTextFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog( editTextFecha);
            }
        });

        final Switch mySwitch = v.findViewById( R.id.switch_prestamo_ingreso);
        final EditText editTextImporte = v.findViewById( R.id.editText_new_item_importe);
        final EditText editTextDescripcion = v.findViewById( R.id.editText_new_item_descripcion);
        final EditText editTextNota = v.findViewById( R.id.editText_new_item_nota);

        mySwitch.setOnCheckedChangeListener( new Switch.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mySwitch.setText(getString(b ? R.string.new_item_ingreso : R.string.new_item_prestamo));
            }
        } );
        
        builder.setView(v);

        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addNewItem( editTextFecha.getText().toString(),
                                    editTextImporte.getText().toString(),
                                    editTextDescripcion.getText().toString(),
                                    editTextNota.getText().toString(),
                                    mySwitch.isChecked() ? TitiItem.ITEM_TYPE_INGRESO : TitiItem.ITEM_TYPE_PRESTAMO);
                    }
                });

        builder.setNegativeButton("CANCELAR",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText( getActivity(), "Cancel", Toast.LENGTH_SHORT).show();
                    }
                });

        AlertDialog dlg = builder.create();

        dlg.show();
    }

    public void addNewItem( String fecha, String importe, String descripción, String nota, int type) {

    }

    public void refilMokItems() {

        titiItems.add( new TitiItem( Calendar.getInstance().getTimeInMillis(), "Transferencia", 1500000, TitiItem.ITEM_TYPE_PRESTAMO));
        titiItems.add( new TitiItem( Calendar.getInstance().getTimeInMillis(), "Transferencia", 15000, TitiItem.ITEM_TYPE_INGRESO));
        titiItems.add( new TitiItem( Calendar.getInstance().getTimeInMillis(), "Transferencia", 15000, TitiItem.ITEM_TYPE_INGRESO));
        titiItems.add( new TitiItem( Calendar.getInstance().getTimeInMillis(), "Transferencia", 15000, TitiItem.ITEM_TYPE_INGRESO));
        titiItems.add( new TitiItem( Calendar.getInstance().getTimeInMillis(), "Transferencia", 15000, TitiItem.ITEM_TYPE_INGRESO));
        titiItems.add( new TitiItem( Calendar.getInstance().getTimeInMillis(), "Transferencia", 15000, TitiItem.ITEM_TYPE_INGRESO));
        titiItems.add( new TitiItem( Calendar.getInstance().getTimeInMillis(), "Transferencia", 15000, TitiItem.ITEM_TYPE_INGRESO));
        titiItems.add( new TitiItem( Calendar.getInstance().getTimeInMillis(), "Transferencia", 15000, TitiItem.ITEM_TYPE_INGRESO));
        titiItems.add( new TitiItem( Calendar.getInstance().getTimeInMillis(), "Transferencia", 15000, TitiItem.ITEM_TYPE_INGRESO));

    }
}