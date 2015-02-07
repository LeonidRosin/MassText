package com.dallinc.masstexter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Created by dallin on 1/30/15.
 */
public class TemplatesFragment extends Fragment {
    List<Template> templates = Template.listAll(Template.class);
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static TemplatesFragment newInstance(int sectionNumber) {
        TemplatesFragment fragment = new TemplatesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public TemplatesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.templates_fragment, container, false);

        FloatingActionButton clickButton = (FloatingActionButton) rootView.findViewById(R.id.buttonCreateTemplate);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(rootView.getContext(), EditTemplate.class);
                startActivity(intent);
            }
        });

        RecyclerView recList = (RecyclerView) rootView.findViewById(R.id.templateCardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(rootView.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        TemplateAdapter ca = new TemplateAdapter(templates);
        recList.setAdapter(ca);

        return rootView;
    }

    public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> {

        private List<Template> templateList;

        public TemplateAdapter(List<Template> templateList) {
            this.templateList = templateList;
        }

        @Override
        public int getItemCount() {
            return templateList.size();
        }

        @Override
        public void onBindViewHolder(final TemplateViewHolder TemplateViewHolder, int i) {
            final Template template = templateList.get(i);
            TemplateViewHolder.vTitle.setText(template.title);
            String body = template.body;
            template.buildArrayListFromString();
            for(String variable: template.variables) {
                body = body.replaceFirst("¬", variable);
            }
            TemplateViewHolder.vBody.setText(body);
            TemplateViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TemplateViewHolder.itemView.getContext(), EditTemplate.class);
                    intent.putExtra("template_id", template.getId());
                    startActivity(intent);
                }
            });
            TemplateViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(TemplateViewHolder.itemView.getContext());
                    builder.setTitle("Delete Template?");
                    builder.setMessage("Do you want to delete the template \"" + template.title + "\"?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            template.delete();
                            templateList = templates = Template.listAll(Template.class);
                            notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                    return false;
                }
            });
        }

        @Override
        public TemplateViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.template_card_layout, viewGroup, false);
            return new TemplateViewHolder(itemView);
        }

        public class TemplateViewHolder extends RecyclerView.ViewHolder {
            protected TextView vTitle;
            protected TextView vBody;

            public TemplateViewHolder(View v) {
                super(v);
                vTitle =  (TextView) v.findViewById(R.id.templateCardTitle);
                vBody = (TextView)  v.findViewById(R.id.templateCardBody);
            }
        }
    }
}
