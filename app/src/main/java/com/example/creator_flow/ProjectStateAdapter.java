package com.example.creator_flow;

import android.content.Context;
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

    // 💡 ProjectStateItem을 ProjectList 데이터 모델로 변경
    private List<ProjectItem> items = new ArrayList<>();
    private final OnProjectActionListener actionListener;

    // 입력 확정이 되는 클릭 리스너 인터페이스도 ProjectList를 받도록 수정
    public interface OnProjectActionListener {
        void onProjectClick(ProjectItem item);
        void onNewProjectConfirmed(ProjectItem item, int position);
    }

    // 생성자
    public ProjectStateAdapter(OnProjectActionListener actionListener) {
        this.actionListener = actionListener;
    }

    // 데이터 교체 함수
    public void setItems(List<ProjectItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    // 아이템 추가 함수
    public void addItem(ProjectItem item) {
        this.items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    // 아이템 삭제 함수 (외부 위임용 안전한 제거 함수)
    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
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
        holder.bind(currentItem, this, actionListener);
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
        public void bind(ProjectItem item, ProjectStateAdapter adapter, OnProjectActionListener listener) {

            // 새 프로젝트 생성을 위해 임시로 생성된 경우 (id가 비어있음)
            if (item.getId() == null || item.getId().isEmpty()) {
                tvProjectName.setVisibility(View.INVISIBLE);
                editProjectName.setVisibility(View.VISIBLE);
                editProjectName.setText(item.getName());
                editProjectName.requestFocus();

                // 키보드 자동으로 올리기
                InputMethodManager imm = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editProjectName, InputMethodManager.SHOW_IMPLICIT);
                }

                // 포커스를 잃었을 때 입력 완료 처리
                editProjectName.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        handleInputComplete(editProjectName.getText().toString().trim(), item, adapter, listener);
                    }
                });
            } else {
                // 이미 생성 완료된 일반 프로젝트 카드인 경우
                tvProjectName.setVisibility(View.VISIBLE);
                editProjectName.setVisibility(View.INVISIBLE);
                tvProjectName.setText(item.getName());

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onProjectClick(item);
                    }
                });
            }
        }

        // 입력창 작성 완료 또는 공백 시 처리
        private void handleInputComplete(String inputText, ProjectItem item, ProjectStateAdapter adapter, OnProjectActionListener listener) {
            int currentPosition = getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            if (inputText.isEmpty()) {
                // 아무것도 입력 안 하고 나갔으면 리스트에서 임시 아이템 삭제
                adapter.removeItem(currentPosition);
            } else {
                item.setName(inputText);
                if (listener != null) {
                    // 프래그먼트에 알림 (여기서 POST API 연동 수행)
                    listener.onNewProjectConfirmed(item, currentPosition);
                }
            }
        }
    }
}
