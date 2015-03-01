package com.dallinc.masstexter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dallinc.masstexter.helpers.Constants;
import com.dallinc.masstexter.models.Template;
import com.dallinc.masstexter.templates.EditTemplate;
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
        rootView.setFocusableInTouchMode(true);
        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK)
                {
                    MainActivity.switchFragments(0);
                }
                return true;
            }
        });

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        boolean hasSeenExample = prefs.getBoolean(Constants.HAS_SEEN_EXAMPLE_TEMPLATE, false);
        if(!hasSeenExample) {
            Template example1 = Constants.getExample1();
            example1.save();
            templates.add(example1);
            Template example2 = Constants.getExample2();
            example2.save();
            templates.add(example2);
            Template example3 = Constants.getExample3();
            example3.save();
            templates.add(example3);
            Template example4 = Constants.getExample4();
            example4.save();
            templates.add(example4);
            prefs.edit().putBoolean(Constants.HAS_SEEN_EXAMPLE_TEMPLATE, true).commit();
        }

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

        private List<Template> objects;

        public TemplateAdapter(List<Template> objects) {
            this.objects = objects;
        }

        @Override
        public int getItemCount() {
            return objects.size();
        }

        @Override
        public void onBindViewHolder(final TemplateViewHolder TemplateViewHolder, int i) {
            final Template template = objects.get(i);
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
                            objects = templates = Template.listAll(Template.class);
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
                ImageView iv = (ImageView) v.findViewById(R.id.recipientIcon);
                iv.setVisibility(View.GONE); // don't show the user icon on template cards
            }
        }
    }
}
