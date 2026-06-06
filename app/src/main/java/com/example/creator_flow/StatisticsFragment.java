package com.example.creator_flow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.creator_flow.databinding.FragmentStatisticsBinding;
import com.example.creator_flow.network.RetrofitClient;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;

    // 어댑터 선언
    private ProjectStateAdapter planningAdapter;
    private ProjectStateAdapter progressingAdapter;
    private ProjectStateAdapter completedAdapter;
    private TodoAdapter todoAdapter;

    private List<TodoItem> todoList = new ArrayList<>();
    private String projectId = "current_project_id"; // To-Do 등록용 예시 ID

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 리사이클러뷰 및 어댑터 초기화
        initProjectRecyclerViews();
        initTodoRecyclerView();

        // 서버로부터 프로젝트 및 To-Do 목록 로드
        fetchProjectsFromServer();
        fetchTodosFromServer();

        // 프로젝트 추가 버튼 (기획중 상태로 임시 아이템 삽입)
        binding.projectAddBtn.setOnClickListener(v -> {
            ProjectItem tempItem = new ProjectItem("", "", "planning");
            planningAdapter.addItem(tempItem);

            int itemCount = planningAdapter.getItemCount();
            if (itemCount > 0) {
                binding.planningRecycle.scrollToPosition(itemCount - 1);
            }
        });

        // To-Do 추가 버튼
        binding.todoAddBtn.setOnClickListener(v -> {
            TodoItem newItem = new TodoItem("");
            todoList.add(newItem);
            todoAdapter.notifyItemInserted(todoList.size() - 1);
            binding.todoRecycler.scrollToPosition(todoList.size() - 1);
        });
    }

    // ── 프로젝트 리사이클러 뷰 & API ───────────────────────────────────
    private void initProjectRecyclerViews() {
        ProjectStateAdapter.OnProjectActionListener projectActionListener = new ProjectStateAdapter.OnProjectActionListener() {
            @Override
            public void onProjectClick(ProjectItem item) {
                // 1. ProjectPageFragment 인스턴스 생성
                ProjectPageFragment projectPageFragment = ProjectPageFragment.newInstance(item.getId());

                // 2. FragmentTransaction을 사용하여 화면 전환
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, projectPageFragment)
                            .addToBackStack(null) // 뒤로가기
                            .commit();
                }
            }

            @Override
            public void onNewProjectConfirmed(ProjectItem item, int position) {
                updateProjectOnServer(item, position);
            }
        };

        planningAdapter = new ProjectStateAdapter(projectActionListener);
        progressingAdapter = new ProjectStateAdapter(projectActionListener);
        completedAdapter = new ProjectStateAdapter(projectActionListener);

        setupVerticalRecyclerView(binding.planningRecycle, planningAdapter);
        setupVerticalRecyclerView(binding.progressingRecycle, progressingAdapter);
        setupVerticalRecyclerView(binding.completedRecycle, completedAdapter);
    }

    private void setupVerticalRecyclerView(RecyclerView recyclerView, ProjectStateAdapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(12));
    }

    /**
     *  API 1: 서버에서 전체 프로젝트 목록을 조회 (GET /projects)
     */
    private void fetchProjectsFromServer() {
        RetrofitClient.getApi(requireContext()).getProjects().enqueue(new Callback<List<ProjectListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProjectListResponse>> call, @NonNull Response<List<ProjectListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectListResponse> responseList = response.body();

                    List<ProjectItem> planningList = new ArrayList<>();
                    List<ProjectItem> progressingList = new ArrayList<>();
                    List<ProjectItem> completedList = new ArrayList<>();

                    for (ProjectListResponse res : responseList) {
                        if (res.getStatus() != null) {
                            ProjectItem project = new ProjectItem(res.getId(), res.getName(), res.getStatus(), res.getGroups());

                            switch (res.getStatus().toLowerCase()) {
                                case "planning":
                                    planningList.add(project);
                                    break;
                                case "progressing":
                                    progressingList.add(project);
                                    break;
                                case "completed":
                                    completedList.add(project);
                                    break;
                            }
                        }
                    }
                    updateProjectData(planningList, progressingList, completedList);
                } else {
                    Toast.makeText(getContext(), "프로젝트를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProjectListResponse>> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "네트워크 통신 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * API 2: 프로젝트 신규 동기화 및 수정 처리 (PUT /projects/{id})
     */
    private void updateProjectOnServer(ProjectItem item, int position) {
        String targetId = item.getId();

        if (targetId == null || targetId.isEmpty()) {
            targetId = UUID.randomUUID().toString();
            item.setId(targetId);
        }

        RetrofitClient.getApi(requireContext())
                .updateProject(
                        targetId,
                        item.getName(),
                        item.getStatus()
                )
                .enqueue(new Callback<com.example.creator_flow.model.ProjectResponse>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<com.example.creator_flow.model.ProjectResponse> call,
                            @NonNull Response<com.example.creator_flow.model.ProjectResponse> response) {

                        if (!isAdded() || getContext() == null) return;

                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "프로젝트가 반영되었습니다.", Toast.LENGTH_SHORT).show();
                            fetchProjectsFromServer();
                        } else {
                            Toast.makeText(getContext(), "저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            removeFailedItemFromAdapter(item.getStatus(), position);
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<com.example.creator_flow.model.ProjectResponse> call,
                            @NonNull Throwable t) {

                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "네트워크 오류로 저장 실패", Toast.LENGTH_SHORT).show();
                        }

                        removeFailedItemFromAdapter(item.getStatus(), position);
                    }
                });
    }
    private void removeFailedItemFromAdapter(String status, int position) {
        if (status.equals("planning") && planningAdapter != null) planningAdapter.removeItem(position);
        else if (status.equals("progressing") && progressingAdapter != null) progressingAdapter.removeItem(position);
        else if (status.equals("completed") && completedAdapter != null) completedAdapter.removeItem(position);
    }

    public void updateProjectData(List<ProjectItem> planning, List<ProjectItem> progressing, List<ProjectItem> completed) {
        if (planningAdapter != null) planningAdapter.setItems(planning);
        if (progressingAdapter != null) progressingAdapter.setItems(progressing);
        if (completedAdapter != null) completedAdapter.setItems(completed);
    }


    // ── To-Do 리사이클러 뷰 & API ───────────────────────────────────

    private void initTodoRecyclerView() {
        todoAdapter = new TodoAdapter(todoList, new TodoAdapter.OnTodoClickListener() {
            @Override
            public void onCheckChanged(TodoItem item, boolean isChecked) {
                updateTodoStatusOnServer(item, isChecked);
            }

            @Override
            public void onDeleteClicked(TodoItem item) {
                deleteTodoFromServer(item);
            }

            @Override
            public void onNewTodoConfirmed(TodoItem item, int position) {
                createTodoOnServer(item, position);
            }
        });

        binding.todoRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.todoRecycler.setAdapter(todoAdapter);
    }

    /**
     *  API 1: 서버에서 전체 to-do 목록을 조회 (GET /todos)
     */
    private void fetchTodosFromServer() {
        RetrofitClient.getApi(requireContext()).getTodos().enqueue(new Callback<List<TodoListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<TodoListResponse>> call, @NonNull Response<List<TodoListResponse>> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<TodoListResponse> bodyList = response.body();
                    List<TodoItem> loadedTodos = new ArrayList<>();
                    for (TodoListResponse res : bodyList) {
                        TodoItem todo = new TodoItem(res.getContent());
                        todo.setId(res.getId());
                        todo.setProject_id(res.getProjectId());
                        todo.setDone(res.isDone());
                        loadedTodos.add(todo);
                    }
                    updateTodoData(loadedTodos);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TodoListResponse>> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "네트워크 통신 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * API 2: 특정 프로젝트에 To-Do 생성
     */

    private void createTodoOnServer(TodoItem item, int position) {
        CreateTodoRequest request = new CreateTodoRequest(item.getContent());
        RetrofitClient.getApi(requireContext()).createTodo(projectId, request).enqueue(new Callback<TodoListResponse>() {
            @Override
            public void onResponse(@NonNull Call<TodoListResponse> call, @NonNull Response<TodoListResponse> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    TodoListResponse res = response.body();
                    item.setId(res.getId());
                    item.setProject_id(res.getProjectId());
                    item.setDone(res.isDone());
                    Toast.makeText(getContext(), "할 일이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TodoListResponse> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "To-Do 생성 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * API 3: To-Do 수정 및 삭제 업데이트
     */
    private void updateTodoStatusOnServer(TodoItem item, boolean isChecked) {
        UpdateTodoRequest request = new UpdateTodoRequest(item.getContent(), isChecked);
        RetrofitClient.getApi(requireContext()).updateTodo(item.getId(), request).enqueue(new Callback<TodoListResponse>() {
            @Override
            public void onResponse(@NonNull Call<TodoListResponse> call, @NonNull Response<TodoListResponse> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful()) {
                    item.setDone(isChecked);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TodoListResponse> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "상태 변경 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * API 4: To-Do 삭제
     */
    private void deleteTodoFromServer(TodoItem item) {
        RetrofitClient.getApi(requireContext()).deleteTodo(item.getId()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful()) {
                    todoList.remove(item);
                    todoAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateTodoData(List<TodoItem> newTodoList) {
        if (newTodoList != null) {
            this.todoList = new ArrayList<>(newTodoList);
            if (todoAdapter != null) {
                todoAdapter.updateList(this.todoList);
            }
        }
    }

    private static class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public VerticalSpaceItemDecoration(int space) {
            this.space = space;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

