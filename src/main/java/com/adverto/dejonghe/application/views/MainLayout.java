package com.adverto.dejonghe.application.views;

import com.adverto.dejonghe.application.views.Staff.EmployeeView;
import com.adverto.dejonghe.application.views.Suppliers.SupplierView;
import com.adverto.dejonghe.application.views.articles.ArticleView;
import com.adverto.dejonghe.application.views.articles.ImportArticleView;
import com.adverto.dejonghe.application.views.customers.CustomerView;
import com.adverto.dejonghe.application.views.invoice.NewInvoiceView;
import com.adverto.dejonghe.application.views.invoice.ProformaInvoiceView;
import com.adverto.dejonghe.application.views.workorder.FinishedWorkorderView;
import com.adverto.dejonghe.application.views.workorder.PendingWorkorderView;
import com.adverto.dejonghe.application.views.workorder.WorkorderView;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * The main view is a top-level placeholder for other views.
 */
@Slf4j
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private H1 viewTitle;

    private HorizontalLayout navbar;

    SideNav nav;

    public MainLayout() {
        setUpNavBar();
        setPrimarySection(Section.DRAWER);
        addHeaderContent();
        addDrawerContent();
        this.navbar.setSpacing(false);
    }

    private void setUpNavBar() {
        navbar = new HorizontalLayout();
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.setMargin(false);
        navbar.setSizeFull();
        navbar.setSpacing(false);
        getElement().executeJs("""
            this.shadowRoot.querySelector('[part="navbar"]').style.backgroundColor = "#223348";
            this.shadowRoot.querySelector('[part="navbar"]').style.color = "white";
        """);
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().getStyle().set("color", "#ffffff");
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.getElement().getStyle().set("color", "#ffffff");
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        viewTitle.setSizeFull();
        viewTitle.setWhiteSpace(HasText.WhiteSpace.NOWRAP);
        navbar.add(toggle, viewTitle);
        addToNavbar(true, navbar);

    }

    private void addDrawerContent() {
        Image logo = new Image("icons/img.png", "Logo");
        logo.setWidth("100%");
        logo.setHeight("100%");

        Header header = new Header(logo);
        header.getElement().getStyle().set("background-color", "#223348");

        Scroller scroller = new Scroller(createNavigation());
        scroller.getElement().setAttribute("theme", Lumo.DARK);

        addToDrawer(header,scroller);
        this.viewTitle.addClickListener(event -> {
            Notification notification = new Notification("Hide navbar!!!");
            notification.open();
            setDrawerOpened(false);
        });
    }

    private SideNav createNavigation() {
        nav = new SideNav();
        SideNavItem importProductLink = new SideNavItem("Import Artikelen",
                ImportArticleView.class, VaadinIcon.COG.create());
        importProductLink.addClassName("sidenav-button");
        SideNavItem invoiceLink = new SideNavItem("Facturatie",
                NewInvoiceView.class, VaadinIcon.EURO.create());
        invoiceLink.addItem(new SideNavItem("Nieuw", NewInvoiceView.class,
                VaadinIcon.WRENCH.create()));
        invoiceLink.addItem(new SideNavItem("Pro forma", ProformaInvoiceView.class,
                VaadinIcon.WRENCH.create()));
        invoiceLink.addItem(new SideNavItem("Facturen", NewInvoiceView.class,
                VaadinIcon.WRENCH.create()));
        SideNavItem workOrderLink = new SideNavItem("Werkbon");
        workOrderLink.addItem(new SideNavItem("Nieuw", WorkorderView.class,
                VaadinIcon.WRENCH.create()));
        workOrderLink.addItem(new SideNavItem("Openstaand", PendingWorkorderView.class,
                VaadinIcon.WRENCH.create()));
        workOrderLink.addItem(new SideNavItem("Afgewerkt", FinishedWorkorderView.class,
                VaadinIcon.WRENCH.create()));

        SideNavItem productLink = new SideNavItem("Artikelen",
                ArticleView.class, VaadinIcon.TOOLBOX.create());

        SideNavItem customerLink = new SideNavItem("Klanten",
                CustomerView.class, VaadinIcon.CLIPBOARD_USER.create());

        SideNavItem technicianLink = new SideNavItem("Techniekers",
                EmployeeView.class, VaadinIcon.WRENCH.create());

        SideNavItem supplierLink = new SideNavItem("Leveranciers",
                SupplierView.class, VaadinIcon.TRUCK.create());

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
