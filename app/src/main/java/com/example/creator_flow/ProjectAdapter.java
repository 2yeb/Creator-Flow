package com.example.creator_flow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public interface OnProjectClickListener {
        void onProjectClick(ProjectItem project);
    }

    public interface OnProjectLongClickListener {
        void onProjectLongClick(ProjectItem project, String currnetGroupId);
    }

    private final List<ProjectItem> projects = new ArrayList<>();
    private final OnProjectClickListener listener;
    private OnProjectLongClickListener longClickListener;
    private String currentGroupId;

    public ProjectAdapter(OnProjectClickListener listener) {
        this.listener = listener;
    }

    public void setOnProjectLongClickListener(OnProjectLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setCurrentGroupId(String groupId) {
        this.currentGroupId = groupId;
    }

    /**
     * 외부(GroupAdapter)에서 호출하여 기존 리스트를 비우고 새로운 데이터셋으로 갈아 끼우기
     */
    public void updateData(List<ProjectItem> newProjects) {
        this.projects.clear();
        if (newProjects != null) {
            this.projects.addAll(newProjects);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_list_project을 inflate
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(projects.get(position), listener, longClickListener, currentGroupId);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout btnProject;
        private final TextView projectName;
        private final TextView tvProjectState;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);

            btnProject = itemView.findViewById(R.id.project_btn);
            projectName = itemView.findViewById(R.id.project_name);
            tvProjectState = itemView.findViewById(R.id.project_state_tv);
        }

        public void bind(final ProjectItem project, final OnProjectClickListener listener,
                         OnProjectLongClickListener longClickListener, String currentGroupId) {
            // 1. 프로젝트 이름 연동
            projectName.setText(project.getName());

            // 2. 프로젝트 상태 불러오기
            tvProjectState.setText(project.getStatus());

            // 3. 클릭 리스너 설정
            btnProject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectClick(project);
                }
            });

            btnProject.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onProjectLongClick(project, currentGroupId);
                    return true; // 일반 클릭이 실행되지 않도록 차단
                }
                return false;
            });
        }
    }
}
