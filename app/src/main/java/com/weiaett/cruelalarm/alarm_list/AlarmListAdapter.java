package com.weiaett.cruelalarm.alarm_list;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.weiaett.cruelalarm.R;
import com.weiaett.cruelalarm.graphics.ColorCircleDrawable;
import com.weiaett.cruelalarm.models.Alarm;
import com.weiaett.cruelalarm.photo_manager.PhotoManagerFragment;
import com.weiaett.cruelalarm.utils.Utils;
import com.weiaett.cruelalarm.utils.Weekday;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.weiaett.cruelalarm.R.id.tvTime;

/**
 * Created by Weiss_A on 26.09.2016.
 * Adapter for alarm list
 */

class AlarmListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_EMPTY_LIST_PLACEHOLDER = 0;
    private static final int VIEW_TYPE_LIST_VIEW = 1;

    private List<Alarm> alarms;
    private SparseBooleanArray selectedItems;

    private Context context;
    private RecyclerView recyclerView;

    private int waitingForToneAlarmPosition = -1;
    private int expandedAlarmPosition = -1;

    AlarmListAdapter(List<Alarm> alarms, RecyclerView recyclerView) {
        this.alarms = alarms;
        this.recyclerView = recyclerView;
        selectedItems = new SparseBooleanArray();
        Utils.sortAlarms(alarms);
    }

    @Override
    public int getItemViewType(int position) {
        return alarms.isEmpty() ? VIEW_TYPE_EMPTY_LIST_PLACEHOLDER : VIEW_TYPE_LIST_VIEW;
    }

    @Override
    public int getItemCount() {
        return alarms.isEmpty() ? 1 : alarms.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view;
        switch(viewType) {
            case VIEW_TYPE_EMPTY_LIST_PLACEHOLDER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.empty_alarm_list_placeholder, parent, false);
                return new EmptyViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_alarm, parent, false);
                return new ListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch(getItemViewType(position)) {
            case VIEW_TYPE_EMPTY_LIST_PLACEHOLDER:
                break;
            case VIEW_TYPE_LIST_VIEW:
                ListViewHolder listViewHolder = (ListViewHolder) holder;
                listViewHolder.item = alarms.get(position);
                listViewHolder.parentView.setActivated(selectedItems.get(position, false));
                listViewHolder.parentView.setBackground(isSelectionMode() ?
                        context.getResources().getDrawable(R.drawable.alarm_card_background_action_mode) :
                        context.getResources().getDrawable(R.drawable.alarm_card_background));
                listViewHolder.activateAlarmView.setColorFilter(listViewHolder.item.getIsActive() ?
                        context.getResources().getColor(R.color.colorAccent) :
                        context.getResources().getColor(R.color.colorNotActive));
                listViewHolder.clearAlarmView.setClickable(!isSelectionMode());
                listViewHolder.clearAlarmView.setVisibility(isSelectionMode() ? View.INVISIBLE : View.VISIBLE);
                listViewHolder.expandAlarmView.setVisibility(isSelectionMode() ? View.INVISIBLE : View.VISIBLE);
                listViewHolder.timeView.setText(listViewHolder.item.getTime());
                listViewHolder.toneView.setText(listViewHolder.item.getTone());
                listViewHolder.descriptionView.setText(listViewHolder.item.getDescription());
                listViewHolder.vibrationView.setChecked(listViewHolder.item.getHasVibration());
                bindActiveDayView(listViewHolder);
                bindPalette(listViewHolder);
                if (expandedAlarmPosition == position && listViewHolder.expandableViewPart.getVisibility() == View.GONE) {
                    Utils.expand(listViewHolder.expandableViewPart);
                    listViewHolder.expandAlarmView.setVisibility(View.INVISIBLE);
                } else if (expandedAlarmPosition != position && listViewHolder.expandableViewPart.getVisibility() == View.VISIBLE) {
                    listViewHolder.expandableViewPart.setVisibility(View.GONE);
                }
        }
    }

    private void bindActiveDayView(ListViewHolder holder) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (!holder.item.getDays().isEmpty()) {
            if (holder.item.getDays().size() == 7) {
                holder.activeDayView.setText(R.string.label_every_day);
            } else {
                for (Weekday day : Weekday.values()) {
                    int colorId = holder.item.getDays().contains(day) ?
                            R.color.colorAccent : R.color.colorNotActive;
                    SpannableString simpleSpannable = new SpannableString(String.format("%-6s", day.toString()));
                    simpleSpannable.setSpan(new ForegroundColorSpan(context.getResources().
                            getColor(colorId)), 0, day.toString().length(), 0);
                    builder.append(simpleSpannable);
                }
                holder.activeDayView.setText(builder, TextView.BufferType.SPANNABLE);
            }
        }
        else {
            if (holder.item.getTime().
                    compareTo(Utils.getFormattedTime(context, Calendar.getInstance())) > 0) {
                holder.activeDayView.setText(R.string.label_today);
            }
            else {
                holder.activeDayView.setText(R.string.label_tomorrow);
            }
        }
    }

    private void bindPalette(ListViewHolder holder) {
        for (Weekday day : Weekday.values()) {
            TextView tvWeekday = (TextView) holder.paletteView.getChildAt(day.ordinal());
            setPaletteItemAppearance(holder.item.getDays().contains(day), tvWeekday);
        }
    }

    private void setPaletteItemAppearance(boolean isActive, TextView tvWeekday) {
        if (isActive) {
            tvWeekday.setBackground(new ColorCircleDrawable(context.getResources().
                    getColor(R.color.colorAccent)));
            tvWeekday.setTextColor(Color.WHITE);
        } else {
            tvWeekday.setBackground(null);
            tvWeekday.setTextColor(context.getResources().getColor(R.color.colorNotActive));
        }
    }

    private void reorderAllAndAccentOne(Alarm alarm) {
        Utils.sortAlarms(alarms);
        notifyDataSetChanged();
        int position = alarms.indexOf(alarm);
        expandedAlarmPosition = position;
        recyclerView.smoothScrollToPosition(position);
    }

    private void deleteAlarm(Alarm alarm) {
        alarm.deleteFromDatabase();
        notifyItemRemoved(alarms.indexOf(alarm));
        alarms.remove(alarm);
    }

    void deleteAlarm(int position) {
        deleteAlarm(alarms.get(position));
    }

    void addAlarm(Alarm alarm) {
        Utils.callAlarmScheduleService(context);
        alarms.add(alarm);
        reorderAllAndAccentOne(alarm);
    }

    void setAlarmParams(int position, Uri toneUri, String tone, boolean hasVibration) {
        Alarm alarm = alarms.get(position);
        if (toneUri != null && tone != null) {
            alarm.setToneUri(toneUri);
            alarm.setTone(tone);
        }
        alarm.setHasVibration(hasVibration);
        alarm.updateAttributesInDatabase();
    }

    void setAlarmParams(int position, Uri toneUri, String tone, boolean hasVibration, List<String> images) {
        Alarm alarm = alarms.get(position);
        alarm.setImages(images);
        if (images.isEmpty() && alarm.getIsActive()) {
            Toast.makeText(context, R.string.toast_no_photo, Toast.LENGTH_LONG).show();
            alarm.setIsActive(false);
            alarm.updateInDatabase();
            notifyItemChanged(position);
        }
        setAlarmParams(position, toneUri, tone, hasVibration);
    }

    void setAlarmParams(int alarmId, List<String> images) {
        int position = -1;
        for (int i = 0; i < alarms.size(); i++) {
            if (alarms.get(i).getId() == alarmId) {
                position = i;
                break;
            }
        }
        if (position < 0) {
            return;
        }
        Alarm alarm = alarms.get(position);
        alarm.setImages(images);
        if (images.isEmpty() && alarm.getIsActive()) {
            Toast.makeText(context, R.string.toast_no_photo, Toast.LENGTH_LONG).show();
            alarm.setIsActive(false);
            alarm.updateInDatabase();
            notifyItemChanged(position);
        }
    }

    void onActivityResult(int resultCode, Intent ringtoneIntent) {
        if (resultCode == RESULT_OK) {
            final Uri uri = ringtoneIntent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            final String ringtoneTitle = RingtoneManager.getRingtone(context, uri).getTitle(context);
            if (waitingForToneAlarmPosition != -1) {
                ListViewHolder holder = (ListViewHolder) recyclerView.findViewHolderForAdapterPosition(waitingForToneAlarmPosition);
                holder.item.setTone(ringtoneTitle);
                holder.item.setToneUri(uri);
                holder.item.updateAttributesInDatabase();
                notifyItemChanged(holder.getAdapterPosition());
            }
        }
    }

    private boolean isSelectionMode() {
        return ((AlarmListActivity) context).isInActionMode();
    }

    void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        }
        else if (position >= 0) {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    void selectAll() {
        for (int i = 0; i < alarms.size(); i++) {
            selectedItems.put(i, true);
        }
        notifyDataSetChanged();
    }

    int getSelectedItemCount() {
        return selectedItems.size();
    }

    int getAlarmsCount() {
        return alarms.size();
    }

    void performClickOnAlarmCard(int position) {
        try {
            toggleExpanding(((ListViewHolder) recyclerView.findViewHolderForAdapterPosition(position)));
        } catch (java.lang.ClassCastException ignored) {} // empty alarm list
    }

    private void toggleExpanding(ListViewHolder holder) {
        if (holder.expandableViewPart.getVisibility() == View.GONE) {
            notifyItemChanged(expandedAlarmPosition);
            expandedAlarmPosition = holder.getAdapterPosition();
            notifyItemChanged(expandedAlarmPosition);
        } else {
            expandedAlarmPosition = -1;
            Utils.collapse(holder.expandableViewPart);
            holder.expandAlarmView.setVisibility(View.VISIBLE);
        }
    }

    void closeExpanded() {
        if (expandedAlarmPosition != -1) {
            Utils.collapse(((ListViewHolder)recyclerView.
                    findViewHolderForAdapterPosition(expandedAlarmPosition)).expandableViewPart);
            expandedAlarmPosition = -1;
        }
    }

    List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    private class ListViewHolder extends RecyclerView.ViewHolder {
        Alarm item;
        final View parentView;
        final View expandableViewPart;
        final LinearLayout paletteView;
        final ImageButton clearAlarmView;
        final ImageView expandAlarmView;
        final ImageView activateAlarmView;
        final TextView timeView;
        final TextView activeDayView;
        final TextView toneView;
        final Switch vibrationView;
        final TextView photoView;
        final EditText descriptionView;

        ListViewHolder(View view) {
            super(view);
            this.parentView = view;
            expandableViewPart = view.findViewById(R.id.incExpanded);
            paletteView = (LinearLayout) view.findViewById(R.id.incDayPalette);
            clearAlarmView = (ImageButton) view.findViewById(R.id.btnClear);
            expandAlarmView = (ImageView) view.findViewById(R.id.imgChevron);
            activateAlarmView = (ImageView) view.findViewById(R.id.imgAlarm);
            timeView = (TextView) view.findViewById(tvTime);
            activeDayView = (TextView) view.findViewById(R.id.tvDays);
            toneView = (TextView) expandableViewPart.findViewById(R.id.tvTone);
            vibrationView = (Switch) expandableViewPart.findViewById(R.id.switchVibration);
            photoView = (TextView) expandableViewPart.findViewById(R.id.tvPhotoPicker);
            descriptionView = (EditText) expandableViewPart.findViewById(R.id.etDescription);
            setupPalette();
            setupListeners();
        }

        void setupListeners() {

            clearAlarmView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteAlarm(item);
                }
            });

            activateAlarmView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!item.getIsActive()) {
                        if (item.getImages().isEmpty()) {
                            Toast.makeText(context, context.getString(R.string.toast_no_photo), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    activateAlarmView.setColorFilter(!item.getIsActive() ?
                            context.getResources().getColor(R.color.colorAccent) :
                            context.getResources().getColor(R.color.colorNotActive));
                    item.setIsActive(!item.getIsActive());
                    item.updateInDatabase();
                }
            });

            descriptionView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                @Override
                public void afterTextChanged(Editable s) {
                    if (!item.getDescription().equals(s.toString())) {
                        item.setDescription(s.toString());
                        item.updateAttributesInDatabase();
                    }
                }
            });

            timeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final Calendar calendar = Calendar.getInstance();
                    final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                            new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    calendar.set(Calendar.MINUTE, selectedMinute);
                                    item.setTime(Utils.getFormattedTime(context, calendar));
                                    timeView.setText(Utils.getFormattedTime(context, calendar));
                                    reorderAllAndAccentOne(item);
                                    item.updateInDatabase();
                                }
                            }, hour, minute, true);
                    timePickerDialog.show();
                }
            });

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment dialogFragment = PhotoManagerFragment.newInstance(item.getId());
//                    dialogFragment.setTargetFragment();
                    FragmentManager fragmentManager = ((AlarmListActivity)context).getSupportFragmentManager();
                    dialogFragment.show(fragmentManager,"Photo Manager");
                }
            });


            toneView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.callTonePicker(context, item.getToneUri(), AlarmListActivity.TONE_PICKER_ROOT_ADAPTER);
                    waitingForToneAlarmPosition = getAdapterPosition();
                }
            });

            vibrationView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (item.getHasVibration() != isChecked) {
                        item.setHasVibration(isChecked);
                        item.updateAttributesInDatabase();
                    }
                }
            });
        }

        void setupPalette() {
            for(int i = 0; i < paletteView.getChildCount(); i++) {
                final Weekday weekday = Weekday.values()[i];
                paletteView.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (item.getDays().contains(weekday)) {
                            AlarmListAdapter.this.setPaletteItemAppearance(false, (TextView) view);
                            item.removeDay(weekday);
                        } else {
                            AlarmListAdapter.this.setPaletteItemAppearance(true, (TextView) view);
                            item.addDay(weekday);
                        }
                        AlarmListAdapter.this.bindActiveDayView(ListViewHolder.this);
                        item.updateInDatabase();
                    }
                });
            }
        }
    }

    private class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(View view) {
            super(view);
        }
    }
}
