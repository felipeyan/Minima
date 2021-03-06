package com.felipeyan.minima;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Pages {
    Data data;

    public static int LIMIT_PER_PAGE = 20;
    public int currentPage, pagesCount, valuesLeft;
    public ArrayList<String> noteIds, noteTexts, noteMods;

    Context context;

    public Pages(Context context) {
        this.data = new Data(context);
        this.context = context;
    }

    public void startValues(int currentPage) {
        setPagesCount((int) Math.floor(data.getDataCount() / LIMIT_PER_PAGE));
        setValuesLeft(data.getDataCount() % LIMIT_PER_PAGE);
        setCurrentPage(currentPage);
        getArraysData(getRangeStart(), endIndexSizeLimiter(getRangeEnd()));
    }

    public void getArraysData(int start, int end) {
        noteIds = new ArrayList<>(data.getNoteIds().subList(start, end));
        noteTexts = new ArrayList<>(data.getNoteTexts().subList(start, end));
        noteMods = new ArrayList<>(data.getNoteMods().subList(start, end));
    }

    public int getRangeStart() {
        return getCurrentPage() * LIMIT_PER_PAGE;
    }

    public int getRangeEnd() {
        return (getCurrentPage() * LIMIT_PER_PAGE) + LIMIT_PER_PAGE;
    }

    public void displayPages() {
        RecyclerView recyclerView = ((MainActivity) context).findViewById(R.id.pagesRV);

        if (data.getDataCount() > LIMIT_PER_PAGE) {
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new Adapter(context, "pages"));
        } else {
            recyclerView.setVisibility(View.GONE);
        }
    }

    public View.OnClickListener pageClick(int pageNum) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) context).currentPage = pageNum;
                ((MainActivity) context).listNotes();
            }
        };
    }

    public int endIndexSizeLimiter(int end) {
        if (end > data.getDataCount()) end = data.getDataCount();
        return end;
    }

    public int getCurrentPage() { return currentPage; }

    public int getPagesCount() { return pagesCount; }

    public int getValuesLeft() { return valuesLeft; }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage >= getPagesCount() && getPagesCount() != 0 ? getPagesCount() - 1 : currentPage;
    }

    public void setPagesCount(int pagesCount) { this.pagesCount = pagesCount; }

    public void setValuesLeft(int valuesLeft) {
        if (valuesLeft != 0) setPagesCount(getPagesCount() + 1);
        this.valuesLeft = valuesLeft;
    }
}
