package com.superg280.dev.titibank;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ArrayList<TitiItem> titiItems = null;
    private ItemTableAdapter adapter = null;

    private DatabaseReference mFirebaseDatabaseItems;
    private ValueEventListener mFireBaseItemsEventListener;

    public boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isLoading = true;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        titiItems = new ArrayList<>();
        adapter = new ItemTableAdapter( this, null);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewItem();

                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mFirebaseDatabaseItems = database.getReference("items");


        //refilMokItems();
    }

    @Override
    public void onResume() {
        super.onResume();
        isLoading = true;
        new WaitTask().execute();
        refillFireBaseItems();
    }

    public void refillMainWindow() {

        adapter = new ItemTableAdapter( this, titiItems);
        ListView lv = (ListView) findViewById(R.id.listView_Items);
        lv.setAdapter(adapter);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                final int posicion = i;
                deleteItemDlg( posicion);
                return true;

            }
        });

        paintResumen();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int position = i;
                showItemDlg( position);

            }
        });
        isLoading = false;
    }

    @Override
    protected void onPause() {
        mFirebaseDatabaseItems.removeEventListener( mFireBaseItemsEventListener);
        super.onPause();
    }

    public void refillFireBaseItems() {

        //mFirebaseDatabaseItems.addValueEventListener(new ValueEventListener() {
        mFireBaseItemsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                titiItems = new ArrayList<>();
                TitiItem it;
                for( DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    it = postSnapshot.getValue( TitiItem.class);
                    titiItems.add(it);
                }

                sortItems();
                refillMainWindow();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                isLoading = false;
                Toast.makeText(MainActivity.this, getString( R.string.connect_fire_error), Toast.LENGTH_LONG).show();
            }
        };
        mFirebaseDatabaseItems.addValueEventListener( mFireBaseItemsEventListener);
    }

    public void deleteItemInFireBase( String gastoId) {

        mFirebaseDatabaseItems.child( gastoId).removeValue();
    }

    public void addItemInFireBase( TitiItem it) {

        mFirebaseDatabaseItems.child( it.getId()).setValue(it);
    }

    public void sortItems() {

        Collections.sort(titiItems, new Comparator<TitiItem>() {
            @Override
            public int compare(TitiItem t1, TitiItem t2) {
                return new Long(t2.getFecha()).compareTo(Long.valueOf(t1.getFecha()));
            }
        });
    }

    public String formatImporte( double importe) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return  nf.format(importe);
    }

    public void paintResumen() {

        paintChartTotal();

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
            AlertDialog.Builder dlgInfo = new AlertDialog.Builder(this);
            dlgInfo.setTitle(getString(R.string.ajustes_mensaje_titulo));
            dlgInfo.setMessage(getString(R.string.ajustes_mensaje_descripcion));
            dlgInfo.setCancelable(false);
            dlgInfo.setPositiveButton(getString(R.string.mensajes_chorras_aceptar), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {

                }
            });
            dlgInfo.show();
            return true;
        }
        if( id == R.id.action_condonar) {
            AlertDialog.Builder dlgInfo = new AlertDialog.Builder(this);
            dlgInfo.setTitle(getString(R.string.condonar_mensaje_titulo));
            dlgInfo.setMessage(getString(R.string.condonar_mensaje_descripcion));
            dlgInfo.setCancelable(false);
            dlgInfo.setPositiveButton(getString(R.string.mensajes_chorras_aceptar), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {

                }
            });
            dlgInfo.show();
            return true;
        }

        if( id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
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

    public void showItemDlg( final int position) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        View v = inflater.inflate(R.layout.show_item_layout, null);

        TitiItem item = titiItems.get( position);

        final TextView twFecha = v.findViewById(R.id.textView_show_fecha);
        final TextView twImporte = v.findViewById(R.id.textView_show_importe);
        final TextView twDescripcion = v.findViewById(R.id.textView_show_descripcion);
        final TextView twNota = v.findViewById(R.id.textView_show_nota);
        final TextView twNotaLabel = v.findViewById(R.id.textView_show_label_nota);
        final ImageView iwTipo = v.findViewById(R.id.imageView_show_icono);
        final TextView twTitulo = v.findViewById(R.id.textView_show_titulo);

        if( item.getItemType() == TitiItem.ITEM_TYPE_PRESTAMO) {
            iwTipo.setImageDrawable(getDrawable(R.drawable.prestamo));
            twImporte.setTextColor(getColor(R.color.colorNegativo));
            twTitulo.setText( getString(R.string.show_item_titulo_prestamo));
        } else {
            iwTipo.setImageDrawable(getDrawable(R.drawable.ingreso));
            twImporte.setTextColor(getColor(R.color.colorPositivo));
            twTitulo.setText( getString(R.string.show_item_titulo_ingreso));
        }

        twFecha.setText(item.formatFecha());
        twImporte.setText(item.formatImporte());
        twDescripcion.setText(item.getDescripcion());
        if( item.hasNota()) {
            twNota.setText(item.getNota());
        } else {
            twNota.setText( getString(R.string.show_item_nota_no_nota));
            twNota.setTextColor( getColor(R.color.colorMuyGrisTextp));
        }

        builder.setView(v);

        AlertDialog dlg = builder.create();

        dlg.show();
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

        builder.setPositiveButton( getString(R.string.new_item_guardar),
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

        builder.setNegativeButton(getString(R.string.new_item_cancelar),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText( getActivity(), "Cancel", Toast.LENGTH_SHORT).show();
                    }
                });

        AlertDialog dlg = builder.create();

        dlg.show();
    }

    public boolean addNewItem( String fecha, String importe, String descripcion, String nota, int type) {

        String fecha_formated = fecha.replace( '/', '-');
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date date;
        try {
            date = sdf.parse(fecha_formated);
        } catch( Exception ex) {
            return false;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        long lImporte;
        try {
            lImporte = (long)(new Double( importe).doubleValue() * 100);
        } catch( Exception ex) {
            return false;
        }

        TitiItem it = new TitiItem( cal.getTimeInMillis(), descripcion, lImporte, type);
        it.setNota( nota);
        addItemInFireBase(it);
        insertNewItemInArray( it);
        adapter.setNewArrayItems( titiItems);
        adapter.notifyDataSetChanged();
        paintResumen();
        return true;
    }

    public void insertNewItemInArray( TitiItem it) {

        if( titiItems.size() == 0) {
            titiItems.add( it);
            return;
        }

        long NewFecha = it.getFecha();
        for( int i = 0; i < titiItems.size(); i++) {

            TitiItem g = titiItems.get( i);
            if( g.getFecha() <= NewFecha) {
                titiItems.add( i, it);
                return;
            }
        }
        //Si ha llegado a salir del bucle es que no ha encontrado
        //una fecha menor y esta es la menor, así que lo mete el último.
        titiItems.add( it);
    }

    public void deleteItemDlg( final int posicion) {

        AlertDialog.Builder dlgBorrar = new AlertDialog.Builder(this);
        dlgBorrar.setTitle(getString(R.string.borrar_item_titulo));
        dlgBorrar.setMessage(getString(R.string.borrar_item_descripcion));
        dlgBorrar.setCancelable(false);
        dlgBorrar.setPositiveButton(getString(R.string.borrar_boton_confirar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                deleteItem(posicion);
            }
        });

        dlgBorrar.setNegativeButton(getString(R.string.borrar_boton_cancelar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
            }
        });
        dlgBorrar.show();
    }

    public void deleteItem( int posicion) {

        deleteItemInFireBase( titiItems.get(posicion).getId());

        titiItems.remove(posicion);

        adapter.setNewArrayItems( titiItems);
        adapter.notifyDataSetChanged();
        paintResumen();
    }

    private class WaitTask extends AsyncTask<Void, Void, Integer>
    {
        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute()
        {
            Dialog.setMessage(getString( R.string.toas_loading_main));
            Dialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params)
        {
            //Task for doing something
            try {
                Calendar inicio = Calendar.getInstance();
                while ( isLoading) {
                    Calendar ahora = Calendar.getInstance();
                    if( ahora.getTimeInMillis() - inicio.getTimeInMillis() > 15000) {
                        return 1;
                    }
                    Thread.sleep(100);
                }
            } catch( Exception ex) {}
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result)
        {

            if(result == 1)
            {
                Toast.makeText(MainActivity.this, getString( R.string.connect_error), Toast.LENGTH_LONG).show();
                finish();
            }
            // after completed finished the progressbar
            Dialog.dismiss();
        }
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
