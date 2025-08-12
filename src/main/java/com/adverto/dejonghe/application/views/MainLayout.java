package com.adverto.dejonghe.application.views;

import com.adverto.dejonghe.application.views.Staff.EmployeeView;
import com.adverto.dejonghe.application.views.Suppliers.SupplierView;
import com.adverto.dejonghe.application.views.articles.ArticleView;
import com.adverto.dejonghe.application.views.articles.ImportArticleView;
import com.adverto.dejonghe.application.views.customers.CustomerView;
import com.adverto.dejonghe.application.views.invoice.NewInvoiceView;
import com.adverto.dejonghe.application.views.workorder.FinishedWorkorderView;
import com.adverto.dejonghe.application.views.workorder.PendingWorkorderView;
import com.adverto.dejonghe.application.views.workorder.WorkorderView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private H1 viewTitle;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("Dejonghe Techniek");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller);
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        SideNavItem importProductLink = new SideNavItem("Import Artikelen",
                ImportArticleView.class, VaadinIcon.COG.create());

        SideNavItem productLink = new SideNavItem("Artikelen",
                ArticleView.class, VaadinIcon.TOOLBOX.create());

        SideNavItem customerLink = new SideNavItem("Klanten",
                CustomerView.class, VaadinIcon.CLIPBOARD_USER.create());

        SideNavItem technicianLink = new SideNavItem("Techniekers",
                EmployeeView.class, VaadinIcon.WRENCH.create());

        SideNavItem invoiceLink = new SideNavItem("Facturatie",
                NewInvoiceView.class, VaadinIcon.EURO.create());
        invoiceLink.addItem(new SideNavItem("Nieuw", NewInvoiceView.class,
                VaadinIcon.WRENCH.create()));
        invoiceLink.addItem(new SideNavItem("Pro forma", NewInvoiceView.class,
                VaadinIcon.WRENCH.create()));
        invoiceLink.addItem(new SideNavItem("Facturen", NewInvoiceView.class,
                VaadinIcon.WRENCH.create()));

        SideNavItem supplierLink = new SideNavItem("Leveranciers",
                SupplierView.class, VaadinIcon.TRUCK.create());

        SideNavItem workOrderLink = new SideNavItem("Werkbon");
        workOrderLink.addItem(new SideNavItem("Nieuw", WorkorderView.class,
                VaadinIcon.WRENCH.create()));
        workOrderLink.addItem(new SideNavItem("Openstaand", PendingWorkorderView.class,
                VaadinIcon.WRENCH.create()));
        workOrderLink.addItem(new SideNavItem("Afgewerkt", FinishedWorkorderView.class,
                VaadinIcon.WRENCH.create()));

        nav.addItem(importProductLink,productLink, customerLink,technicianLink,invoiceLink,supplierLink,workOrderLink);

//        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
//        menuEntries.forEach(entry -> {
//            if (entry.icon() != null) {
//                nav.addItem(new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon())));
//            } else {
//                nav.addItem(new SideNavItem(entry.title(), entry.path()));
//            }
//        });
        return nav;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}
