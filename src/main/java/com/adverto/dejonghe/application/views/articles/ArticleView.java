package com.adverto.dejonghe.application.views.articles;

import com.adverto.dejonghe.application.dbservices.*;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.views.subViews.SelectProductSubView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.*;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Producten")
@Route("product")
@Menu(order = 0, icon = LineAwesomeIconUrl.COG_SOLID)
public class ArticleView extends Div implements BeforeEnterObserver {
    SelectProductSubView selectProductSubView;
    ProductService productService;

    SplitLayout mainSplitLayout;
    SplitLayout headerSplitLayout;

    public ArticleView(ProductService productService,
                       SelectProductSubView selectProductSubView) {
        this.productService = productService;
        this.selectProductSubView = selectProductSubView;

        selectProductSubView.setUserFunction(UserFunction.WAREHOUSEWORKER);
        setUpSplitLayouts();
        mainSplitLayout.addToPrimary(selectProductSubView.getLayout());
        mainSplitLayout.addToSecondary(headerSplitLayout);
        this.setHeightFull();
        add(mainSplitLayout);
    }

    private void setUpSplitLayouts() {
        mainSplitLayout = new SplitLayout();
        mainSplitLayout.setSizeFull();
        mainSplitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);

        headerSplitLayout = new SplitLayout();
        headerSplitLayout.setSplitterPosition(50);
        headerSplitLayout.setSizeFull();
        headerSplitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);

        headerSplitLayout.addSplitterDragendListener(event -> {
            selectProductSubView.setSplitPosition(headerSplitLayout.getSplitterPosition());
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }
}
