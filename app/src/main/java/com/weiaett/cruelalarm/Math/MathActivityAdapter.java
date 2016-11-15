package com.weiaett.cruelalarm.Math;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.weiaett.cruelalarm.R;

import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by dekib_000 on 12.11.2016.
 */

public class MathActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private RecyclerView recyclerView;
    private String expression;
    private String correctAnswer;
    private String[] answers;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mathAnswer;

        public ViewHolder(View v) {
            super(v);
            mathAnswer = (TextView) v.findViewById(R.id.mathAnswer);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.mathAnswer.setText(answers[position]);
        viewHolder.mathAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder
                        .mathAnswer
                        .getText()
                        .toString()
                        .equals(correctAnswer)) {
                    viewHolder
                            .mathAnswer
                            .setText("Crrect");
                    //  Передача управления куда-то
                } else {
                    shuffleArray(answers);
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return answers.length;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View myView = LayoutInflater.from(context)
                .inflate(R.layout.card_math, parent, false);
        return new ViewHolder(myView);
    }

    MathActivityAdapter(String expression, String[] answers, RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.expression = expression;
        this.answers = answers;
        this.correctAnswer = answers[0];
        shuffleArray(this.answers);
    }

    private void shuffleArray(String[] arr) {
        Random rand = new Random(System.currentTimeMillis());
        for (int i = 0; i < arr.length; i++) {
            int index = rand.nextInt(arr.length);
            String tmp = arr[i];
            arr[i] = arr[index];
            arr[index] = tmp;
        }
    }
}
