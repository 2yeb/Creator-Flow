package com.example.creator_flow;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private List<TodoItem> todoList;
    private final OnTodoClickListener listener;

    public interface OnTodoClickListener {
        void onCheckChanged(TodoItem item, boolean isChecked);
        void onDeleteClicked(TodoItem item);
        void onNewTodoConfirmed(TodoItem item, int position);
    }

    public TodoAdapter(List<TodoItem> todoList, OnTodoClickListener listener) {
        this.todoList = todoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem currentItem = todoList.get(position);
        holder.bind(currentItem, position);
    }

    @Override
    public int getItemCount() {
        return todoList != null ? todoList.size() : 0;
    }

    public void updateList(List<TodoItem> newList) {
        this.todoList = newList;
        notifyDataSetChanged();
    }

    public class TodoViewHolder extends RecyclerView.ViewHolder {
        private final TextView todoContent;
        private final EditText todoEditText;
        private final ImageView todoCheck;
        private final ImageView btnDelete;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            todoContent = itemView.findViewById(R.id.todo_content);
            todoEditText = itemView.findViewById(R.id.todo_editText);
            todoCheck = itemView.findViewById(R.id.todo_check);
            btnDelete = itemView.findViewById(R.id.delete_btn);
        }
        public void bind(final TodoItem item, final int position) {
            // id가 없는 생성 초기 단계 -> EditText 모드
            if (item.getId() == null || item.getId().isEmpty()) {

                todoContent.setVisibility(View.GONE);
                todoCheck.setVisibility(View.INVISIBLE);
                btnDelete.setVisibility(View.INVISIBLE);
                todoEditText.setVisibility(View.VISIBLE);

                todoEditText.setText(item.getContent() != null ? item.getContent() : "");

                // 포커스 및 키보드 활성화
                todoEditText.post(() -> {
                    todoEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(todoEditText, InputMethodManager.SHOW_IMPLICIT);
                    }
                });

                // 포커스가 나갔을 때 처리
                todoEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            String inputText = todoEditText.getText().toString().trim();
                            handleInputComplete(inputText, item, position);
                        }
                    }
                });

                // 엔터 키(완료)를 누르면 포커스를 의도적으로 해제하여
                // 중복 호출 없이 깔끔하게 완료 프로세스를 밟도록 유도
                todoEditText.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                            actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT ||
                            (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {

                        // 키보드를 내리고 포커스를 해제하면 위의 OnFocusChange가 알아서 실행됩니다.
                        todoEditText.clearFocus();

                        InputMethodManager imm = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(todoEditText.getWindowToken(), 0);
                        }
                        return true;
                    }
                    return false;
                });

            } else {
                // id가 존재하는 정식 아이템 단계 -> TextView 모드
                todoCheck.setVisibility(View.VISIBLE);
                todoContent.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                todoEditText.setVisibility(View.GONE);

                todoContent.setText(item.getContent());

                // 1. 초기 UI 상태 반영 (셀렉터의 state_checked 상태 조작 및 취소선 표시)
                updateCheckUI(item.isDone());

                // 2. 체크박스 클릭 이벤트 설정
                todoCheck.setOnClickListener(v -> {
                    // 상태 토글
                    boolean newCheckedState = !item.isDone();
                    item.setDone(newCheckedState);

                    // UI 상태 즉시 업데이트 (체크박스 이미지 변경 및 취소선 토글)
                    updateCheckUI(newCheckedState);

                    // Fragment 측 인터페이스로 이벤트 전달 (DB 등 연동용)
                    if (listener != null) {
                        listener.onCheckChanged(item, newCheckedState);
                    }
                });

                // 삭제 버튼 클릭 이벤트
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClicked(item);
                    }
                });
            }
        }

        // 체크박스 표시 및 취소선, 삭제 버튼 추가 코드
        private void updateCheckUI(boolean isChecked) {
            // ImageView에 state_checked 상태 주입 (selector XML과 연동)
            int[] state = isChecked ? new int[]{android.R.attr.state_checked} : new int[]{-android.R.attr.state_checked};
            todoCheck.setImageState(state, true);

            // 완료된 경우 텍스트에 중간 취소선 추가, 미완료인 경우 취소선 제거
            if (isChecked) {
                todoContent.setPaintFlags(todoContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                todoContent.setAlpha(0.5f);
                btnDelete.setVisibility(View.VISIBLE);
            } else {
                todoContent.setPaintFlags(todoContent.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                todoContent.setAlpha(1.0f);
                btnDelete.setVisibility(View.INVISIBLE);
            }
        }

        // 입력이 끝났을 때 공백 검사 및 저장
        private void handleInputComplete(String inputText, TodoItem item, int position) {
            if (inputText.isEmpty()) {
                todoList.remove(position);
                notifyItemRemoved(position);
            } else {
                item.setContent(inputText);

                if (listener != null) {
                    listener.onNewTodoConfirmed(item, position);
                }
            }
        }
    }
}