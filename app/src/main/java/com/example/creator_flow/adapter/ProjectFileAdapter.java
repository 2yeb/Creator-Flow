package com.example.creator_flow.adapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.creator_flow.R;
import com.example.creator_flow.model.ProjectFile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProjectFileAdapter extends RecyclerView.Adapter<ProjectFileAdapter.ViewHolder> {

    private final List<ProjectFile> fileList;
    private final OnAddChasiListener addChasiListener;
    private int selectedPosition = 0;

    public interface OnAddChasiListener {
        void onAddChasi();
    }

    public ProjectFileAdapter(List<ProjectFile> fileList, OnAddChasiListener listener) {
        this.fileList = fileList;
        this.addChasiListener = listener;
    }

    public void setSelectedPosition(int position) {
        int prev = this.selectedPosition;
        this.selectedPosition = position;
        notifyItemChanged(prev);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_project_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProjectFile file = fileList.get(position);
        Context context = holder.itemView.getContext();

        // 탭 너비: 선택된 탭은 고정, 나머지는 남은 공간 균등 분배
        final float SELECTED_WIDTH = 0.4f;
        int n = fileList.size();
        float widthPercent;
        if (position == selectedPosition || n == 1) {
            widthPercent = SELECTED_WIDTH;
        } else {
            widthPercent = Math.max(0.05f, (1.0f - SELECTED_WIDTH) / (n - 1));
        }
        ConstraintLayout.LayoutParams tabParams =
                (ConstraintLayout.LayoutParams) holder.chasiTabBackground.getLayoutParams();
        tabParams.matchConstraintPercentWidth = widthPercent;
        holder.chasiTabBackground.setLayoutParams(tabParams);

        // chasi_tab: 선택된 페이지에서만 표시
        holder.chasiTab.setText(file.getChasiNumber() + "차시");
        holder.chasiTab.setVisibility(position == selectedPosition ? View.VISIBLE : View.INVISIBLE);

        // 프로젝트명
        if (file.getProjectName() != null) {
            holder.etProjectName.setText(file.getProjectName());
        }

        // 카테고리 스피너
        String[] categories = context.getResources().getStringArray(R.array.category_items);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, categories) {

            @Override
            public boolean isEnabled(int pos) {
                return pos != 0;
            }

            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                View v = super.getView(pos, convertView, parent);
                ((TextView) v).setTextColor(pos == 0 ? 0xAAFFFFFF : 0xFF313131);
                return v;
            }

            @Override
            public View getDropDownView(int pos, View convertView, ViewGroup parent) {
                if (pos == 0) {
                    View v = new View(context);
                    v.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
                    return v;
                }
                View v = super.getDropDownView(pos, convertView, parent);
                ((TextView) v).setTextColor(0xFF313131);
                return v;
            }
        };
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerCategory.setAdapter(spinnerAdapter);

        if (file.getCategory() != null) {
            int idx = spinnerAdapter.getPosition(file.getCategory());
            if (idx >= 0) holder.spinnerCategory.setSelection(idx);
        }

        // 날짜
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
        if (file.getDate() != null && !file.getDate().isEmpty()) {
            holder.tvDate.setText(file.getDate());
        } else {
            holder.tvDate.setText(sdf.format(Calendar.getInstance().getTime()));
        }

        holder.tvDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(context,
                    (datePicker, year, month, day) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, day);
                        String dateStr = sdf.format(selected.getTime());
                        holder.tvDate.setText(dateStr);
                        file.setDate(dateStr);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // + 버튼
        holder.btnAddChasi.setOnClickListener(v -> {
            if (addChasiListener != null) addChasiListener.onAddChasi();
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View chasiTabBackground;
        TextView chasiTab;
        EditText etProjectName;
        Spinner spinnerCategory;
        TextView tvDate;
        ImageButton btnAddChasi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chasiTabBackground = itemView.findViewById(R.id.chasi_tab_background);
            chasiTab           = itemView.findViewById(R.id.chasi_tab);
            etProjectName      = itemView.findViewById(R.id.folder_name);
            spinnerCategory    = itemView.findViewById(R.id.folder_category);
            tvDate             = itemView.findViewById(R.id.folder_calendar);
            btnAddChasi        = itemView.findViewById(R.id.folder_add);
        }
    }
}
