import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import mynamespace.R;

/**
 * AlertDialog.Builder for a TimePicker dialog that supports
 * a margin, minimum time and custom time increases
 */
public class CustomizableTimeMarginDialog 
    extends AlertDialog.Builder implements NumberPicker.OnValueChangeListener{

    /**
     * Separate custom view for AlertDialog title
     */
    private View titleView;

    /**
     * NumberPickers for hour, minutes and margin
     */
    private NumberPicker pickerHour, pickerMinutes, pickerMargin;

    /**
     * Values for all three NumberPickers
     */
    private NumberPickerValues hours, minutes, margins;

    /**
     * Minimum possible time, saved separately as hour and minutes
     */
    private int minHour, minMinutes;


    public CustomizableTimeMarginDialog(Context context) {
        super(context);
    }

    public CustomizableTimeMarginDialog(Context context, int theme) {
        super(context, theme);
    }

    /**
     * Create this dialog
     * @param min can be null
     * @param selected
     * @param margin
     * @param listener
     * @param hours
     * @param minutes
     * @param margins
     */
    public void create(Calendar min, Calendar selected, int margin, 
        final OnTimeMarginSetListener listener, 
        NumberPickerValues hours, final NumberPickerValues minutes, NumberPickerValues margins) {

        //inflate layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogLayout = inflater.inflate(R.layout.view_dialog_margintime, null);
        titleView = inflater.inflate(R.layout.view_title_margintime, null);

        pickerHour = (NumberPicker) dialogLayout.findViewById(R.id.picker_hour);
        pickerMinutes = (NumberPicker) dialogLayout.findViewById(R.id.picker_minutes);
        pickerMargin = (NumberPicker) dialogLayout.findViewById(R.id.picker_margin);

        this.hours = hours;
        this.minutes = minutes;
        this.margins = margins;

        //if no minimum time given, set it to 00:00
        if(min == null){
            min = Calendar.getInstance();
            min.set(Calendar.HOUR_OF_DAY, 0);
            min.set(Calendar.MINUTE, 0);
        }

        //save minimum time
        minHour = min.get(Calendar.HOUR_OF_DAY);
        minMinutes = min.get(Calendar.MINUTE);

        //initialize NumberPicker values
        initPicker(pickerHour, this.hours, selected.get(Calendar.HOUR_OF_DAY));
        initPicker(pickerMinutes, this.minutes, selected.get(Calendar.MINUTE));
        initPicker(pickerMargin, this.margins, margin);
        pickerMargin.setWrapSelectorWheel(false);

        //initialize NumberPicker listeners
        pickerHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                int newMinMinutes = newVal == minHour ? minMinutes : minutes.min;
                pickerMinutes.setMinValue(minutes.getClosestIndex(newMinMinutes));
                updateTitle();
            }
        });
        pickerMinutes.setOnValueChangedListener(this);
        pickerMargin.setOnValueChangedListener(this);

        //build dialog
        this.setView(dialogLayout)
                .setCustomTitle(titleView)
                .setPositiveButton(
                    "Set",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            listener.OnTimeMarginSet(getHour(), getMinutes(), getMargin());
                        }
                })
                .setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                });
        this.create();
        updateTitle();
    }
    //use .show() afterwards to draw and use this dialog

    /**
     * Initialize NumberPicker with possible values and a selected one
     * @param picker
     * @param values
     * @param selected
     */
    private void initPicker(NumberPicker picker, NumberPickerValues values, int selected) {
        if (values.min <= selected && selected <= values.max) {
            picker.setMinValue(values.getClosestIndex(values.min));
            picker.setMaxValue(values.getClosestIndex(values.max));
            picker.setDisplayedValues(values.valuesAsStrings);
            picker.setValue(values.getClosestIndex(selected));
        }
    }

    /**
     * Update dialog title with selected time(s)
     */
    private void updateTitle(){
        if (titleView != null) {
            ((TextView) titleView.findViewById(R.id.textView)).setText(getTitle());
        }
    }

    /**
     * Generate String for title from selected time(s)
     * @return String representation of time or timerange
     */
    private String getTitle(){
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, getHour());
        start.set(Calendar.MINUTE, getMinutes());
        return CalendarUtil.getTimeRangeString(start, Calendar.MINUTE, getMargin());
    }

    /**
     * Get selected hour
     * @return hour selected
     */
    private int getHour(){
        return hours.values.get(pickerHour.getValue());
    }

    /**
     * Get selected minutes
     * @return minutes selected
     */
    private int getMinutes(){
        return minutes.values.get(pickerMinutes.getValue());
    }

    /**
     * Get selected margin
     * @return margin selected
     */
    private int getMargin(){
        return margins.values.get(pickerMargin.getValue());
    }

    /**
     * When a NumberPicker changes value, update title
     * @param numberPicker
     * @param oldVal
     * @param newVal
     */
    @Override
    public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
        updateTitle();
    }


    /*** Nested Classes ***/

    /**
     * Custom listener fired when dialog's positive button is pushed
     */
    public interface OnTimeMarginSetListener{
        public void OnTimeMarginSet(int hour, int minutes, int margin);
    }

    /**
     * Wrapper around String[] of Integers, for NumberPicker DisplayedValues
     */
    public static class NumberPickerValues{
        /**
         * Numerical values
         */
        private ArrayList<Integer> values;

        /**
         * Values as Strings
         */
        private String[] valuesAsStrings;

        /**
         * Amount of values
         */
        private int length;

        /**
         * Bounds for values
         */
        private int min, max, step;

        public NumberPickerValues(int min, int max, int step){
            this.min = min;
            this.max = max;
            this.step = step;

            //calculate length
            length = max - min;
            if (step > 0) length /= step;
            length++;

            //generate values
            values = new ArrayList<>(length);
            valuesAsStrings = new String[length];
            for (int i = 0, val = min; i < length && val <= max; i++, val += step) {
                values.add(i, val);
                valuesAsStrings[i] = val + "";
            }
        }

        /**
         * Get index for exact value or member of collection closest to value
         * @param value
         * @return index in collection
         */
        private int getClosestIndex(int value){
            return (value - min) / step;
        }
    }
}
