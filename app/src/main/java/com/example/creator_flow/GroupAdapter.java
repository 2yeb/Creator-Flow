package com.example.creator_flow;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final List<GroupItem> groups;
    private final ProjectAdapter.OnProjectClickListener projectClickListener;

    public GroupAdapter(List<GroupItem> groups, ProjectAdapter.OnProjectClickListener projectClickListener) {
        this.groups = groups;
        this.projectClickListener = projectClickListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_group, parent, false);
        return new GroupViewHolder(view, projectClickListener); // 뷰홀더 생성 시점에 리스너 패스
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.bind(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups != null ? groups.size() : 0;
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        CardView groupCardView;
        ImageView circleIv, dropdownIv;
        TextView groupNameTv;
        RecyclerView childRecyclerView;

        // 하위 어댑터 재사용을 위해 뷰홀더의 멤버 변수로 승격시킵니다.
        ProjectAdapter projectAdapter;

        public GroupViewHolder(@NonNull View itemView, ProjectAdapter.OnProjectClickListener listener) {
            super(itemView);
            groupCardView = itemView.findViewById(R.id.group_btn);
            circleIv = itemView.findViewById(R.id.circle_iv);
            groupNameTv = itemView.findViewById(R.id.group_name_tv);
            dropdownIv = itemView.findViewById(R.id.dropdown_iv);
            childRecyclerView = itemView.findViewById(R.id.child_project_recycler);

            // 뷰홀더가 메모리에 처음 잡힐 때 딱 한 번만 레이아웃 매니저와 세로정렬 세팅
            Context context = itemView.getContext();
            childRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

            // 리사이클러뷰 아이템 간격 7dp로 조정
            int spaceInPx = (int) (7 * context.getResources().getDisplayMetrics().density); // 7dp 간격
            childRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(spaceInPx));

            projectAdapter = new ProjectAdapter(listener);
            childRecyclerView.setAdapter(projectAdapter);

            childRecyclerView.setHasFixedSize(true);
            childRecyclerView.setNestedScrollingEnabled(false);
        }

        public void bind(final GroupItem group) {
            Context context = itemView.getContext();

            groupNameTv.setText(group.getKeyword());

            int themeColor = ContextCompat.getColor(context, group.getColorResId());
            groupCardView.setCardBackgroundColor(ColorStateList.valueOf(themeColor));
            circleIv.setImageTintList(ColorStateList.valueOf(themeColor));

            // ProjectAdapter를 인스턴스화하지 않고, 중복 생성 방지
            projectAdapter.updateData(group.getProjects());

            // 드롭다운 상태 적용
            if (group.isExpanded()) {
                childRecyclerView.setVisibility(View.VISIBLE);
                dropdownIv.setRotation(0f);
            } else {
                childRecyclerView.setVisibility(View.GONE);
                dropdownIv.setRotation(180f);
            }

            // 클릭 시 드롭다운 토글
            groupCardView.setOnClickListener(v -> {
                boolean nextState = !group.isExpanded();
                group.setExpanded(nextState);

                childRecyclerView.setVisibility(nextState ? View.VISIBLE : View.GONE);
                dropdownIv.setRotation(nextState ? 0f : 180f);
            });
        }
    }

    // child_project_recycler 내부 아이템 간격 조정
    private static class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public VerticalSpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = space; // 마지막 아이템이 아니면 아래에 마진 부여
            }
        }
    }
}
