package com.example.creator_flow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProjectStateAdapter extends RecyclerView.Adapter<ProjectStateAdapter.ViewHolder> {

    private List<ProjectItem> items = new ArrayList<>();
    private final OnProjectActionListener actionListener;
    private final String state; // 프로젝트 상태를 저장할 변수

    // 클릭 및 수정 인터페이스
    public interface OnProjectActionListener {
        void onProjectClick(ProjectItem item);
        void onProjectUpdated(ProjectItem item, int position);
    }

    // 생성자
    public ProjectStateAdapter(String state, OnProjectActionListener actionListener) {
        this.state = state;
        this.actionListener = actionListener;
    }

    // 데이터 교체 함수
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<ProjectItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_project_state, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProjectItem currentItem = items.get(position);
        holder.bind(currentItem, this, state,  actionListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView btnProjectItem;
        private final TextView tvProjectName;
        private final EditText editProjectName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btnProjectItem = itemView.findViewById(R.id.project_item_btn);
            tvProjectName = itemView.findViewById(R.id.project_name_tv);
            editProjectName = itemView.findViewById(R.id.project_name_edit);
        }

        // ProjectList 바인딩 로직
        public void bind(ProjectItem item, ProjectStateAdapter adapter, String state, OnProjectActionListener listener) {

            // 기본 상태 설정
            tvProjectName.setVisibility(View.VISIBLE);
            editProjectName.setVisibility(View.GONE);
            tvProjectName.setText(item.getName());

            // 상태에 따른 색상 설정
            if (state != null) {
                Context context = itemView.getContext();
                switch (state) {
                    case "planning":
                        btnProjectItem.setCardBackgroundColor(context.getColor(R.color.group_yellow));
                        break;
                    case "progressing":
                        btnProjectItem.setCardBackgroundColor(context.getColor(R.color.group_pink));
                        break;
                    case "completed":
                        btnProjectItem.setCardBackgroundColor(context.getColor(R.color.group_blue));
                        break;
                    default:
                        btnProjectItem.setCardBackgroundColor(context.getColor(R.color.group_yellow));
                        break;
                }
            }

            // 아이템 클릭 시 해당 project_page로 이동
            btnProjectItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectClick(item);
                }
            });

            // 아이템을 길게 클릭하면 프로젝트의 이름 변경
            btnProjectItem.setOnLongClickListener(v -> {
                tvProjectName.setVisibility(View.INVISIBLE);
                editProjectName.setVisibility(View.VISIBLE);
                editProjectName.setText(item.getName());
                editProjectName.requestFocus();

                InputMethodManager imm = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editProjectName, InputMethodManager.SHOW_IMPLICIT);
                }
                return true;
            });

            // 포커스를 잃었을 때 수정 완료 처리
            editProjectName.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    handleInputComplete(editProjectName.getText().toString().trim(), item, adapter, listener);
                }
            });
        }

        // 입력창 작성 완료 또는 공백 시 처리
        private void handleInputComplete(String inputText, ProjectItem item, ProjectStateAdapter adapter, OnProjectActionListener listener) {
            int currentPosition = getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            if (inputText.isEmpty()) {
                // 수정 시 공백으로 두면 기존 이름으로 복구
                tvProjectName.setVisibility(View.VISIBLE);
                editProjectName.setVisibility(View.GONE);
            } else if (!inputText.equals(item.getName())) {
                // 내용이 변경되었을 때만 서버에 업데이트 요청
                item.setName(inputText);
                tvProjectName.setText(inputText);
                tvProjectName.setVisibility(View.VISIBLE);
                editProjectName.setVisibility(View.GONE);
                if (listener != null) {
                    listener.onProjectUpdated(item, currentPosition);
                }
            } else {
                tvProjectName.setVisibility(View.VISIBLE);
                editProjectName.setVisibility(View.GONE);
            }
        }
    }
}
