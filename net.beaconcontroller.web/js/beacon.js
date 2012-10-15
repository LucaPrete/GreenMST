/**
 * Finds the index of the tab the specified element is in. If none is found
 * this function returns null.
 * @param element
 * @returns
 */
function findTabIndex(element) {
    var parents = $(element).parents('.ui-tabs-panel');
    var index = null;
    if (parents.size() > 0) {
        var tabId = parents.attr('id');
        var nav = $('.ui-tabs-nav');
        $(nav).find('a').each(function (idx, element) {
            if ($(element).attr('href') == ('#'+tabId)) {
                index = $(nav).children().index($(this).parent());
            }
        });
    }
    return index;
}

/**
 * Returns the index for the clicked on tab if clicking on the close X for a tab
 * @param element
 * @returns
 */
function findTabIndexByTab(element) {
    var nav = $('.ui-tabs-nav');
    var index = $(nav).children().index($(element).parent());
    return index;
}

/**
 * Return the tabs object
 * @returns
 */
function getTabs() {
  return $('#center').tabs();
}

/**
 * Returns the element that is clicked to close the tab
 * @param index
 * @returns
 */
function getTabCloseElement(index) {
    var tab = $('.ui-tabs-nav').children()[index];
    var span = $(tab).find('span')[0];
    return span;
}

/**
 * Returns the index of the currently selected tab
 */
function getTabSelectedIndex() {
    return $("#center").tabs("option", "selected");
}

/**
 * Sets the index of the selected tab
 * @param index
 */
function setTabsSelectedIndex(index) {
    $("#center").tabs("option", "selected", index);
}

/**
 * Returns the number of tabs available for selection
 */
function getTabsLength() {
    return $("#center").tabs("length");
}

/**
 * 
 * @param tableId
 * @param ajaxURL
 * @param tableOptions
 * @returns {DataTableWrapper}
 */
function DataTableWrapper(tableId, ajaxURL, tableOptions, refreshButton, autoRefreshButton) {
    var that = this;
    refreshButton = typeof(refreshButton) != 'undefined' ? refreshButton : true;
    autoRefreshButton = typeof(autoRefreshButton) != 'undefined' ? autoRefreshButton : true;
    //$('#table-flows-new').addClass(switchId);
    $('#'+tableId).addClass(tableId);
    //$('#table-flows-new').attr('id', 'table-flows-'+switchIdEsc);
    var options = $.extend({}, {
        "bFilter": false,
        "bJQueryUI": true,
        "bPaginate": false,
        "bRetrieve": true,
        "bSort": true,
        "bStateSave": true,
        "iCookieDuration": 365*24*60*60, /* 1 year */
        //"oSearch": {"sSearch": "", "bRegex": false, "bSmart": true },
        "sCookiePrefix": tableId,
        "sDom": '<"H"lfr>t<"F"ip>'
    }, tableOptions);
    if (ajaxURL != null) {
      options = $.extend(options, {"sAjaxSource": ajaxURL});
    }
    this.table = $('#'+tableId).dataTable(options);
    this.timerId = null;
    var filterBar = $('#'+tableId+'_filter');

    // Have to add this by creating elements else the input field gets re-evaluated and loses its event handlers
    var button;
    if (refreshButton) {
        button = document.createElement("button");
        $(button).attr("id", tableId+"-button-refresh");
        $(button).attr("style", "margin-left: 5px;");
        $(button).html("Refresh");
        $(filterBar).append(button);
        this.buttonRefresh = $('#'+tableId+'-button-refresh').button({ icons: {primary:'ui-icon-arrowrefresh-1-e'} });
        this.buttonRefresh.click(function() { that.table.fnReloadAjax(); });
    }

    if (autoRefreshButton) {
        button = document.createElement("button");
        $(button).attr("id", tableId+"-button-autorefresh");
        $(button).attr("style", "margin-left: 5px;");
        $(button).html("Auto Refresh");
        $(filterBar).append(button);
        //filterBar.html('<button id="button-refresh-'+switchIdEsc+'" style="margin-left: 5px;">Refresh</button>' + filterBar.html());
//    filterBar.html(filterBar.html() + '<button id="button-autorefresh-'+switchIdEsc+'" style="margin-left: 5px;">Start Auto Refresh</button>');
        this.buttonAutoRefresh = $('#'+tableId+'-button-autorefresh').button({ icons: {primary:'ui-icon-arrowrefresh-1-e'} });
        this.buttonAutoRefresh.click(function () { that.startAutoRefresh(); });
    }

    // Add a close handler to the tab to remove the timer
    var closer = getTabCloseElement(findTabIndex(this.table));
    $(closer).click(function() {
        if (that.timerId != null) {
            clearInterval(that.timerId);
        }
    });
}

DataTableWrapper.prototype.startAutoRefresh = function startAutoRefresh() {
    var that = this;
    this.timerId = setInterval(function () {
        that.table.fnReloadAjax();
    }, 5000);
    this.buttonAutoRefresh.button("option", "label", "Stop Auto Refresh");
    this.buttonAutoRefresh.button("option", "icons", {primary:'ui-icon-close'});
    this.buttonAutoRefresh.unbind('click');
    this.buttonAutoRefresh.click(function () { that.stopAutoRefresh(); });
}

DataTableWrapper.prototype.stopAutoRefresh = function startAutoRefresh() {
    var that = this;
    if (this.timerId != null) {
        clearInterval(this.timerId);
    }
    this.buttonAutoRefresh.button("option", "label", "Start Auto Refresh");
    this.buttonAutoRefresh.button("option", "icons", {primary:'ui-icon-arrowrefresh-1-e'});
    this.buttonAutoRefresh.unbind('click');
    this.buttonAutoRefresh.click(function () { that.startAutoRefresh(); });
    this.timerId = null;
}

$(document).bind('keystrokes.OtherNameSpace', [
       {
           keys: ['j'],
           proceedToMainCallback: false,
           success: function(event) {
               var index = getTabSelectedIndex();
               --index;
               if (index < 0)
                   index = getTabsLength()-1;
               setTabsSelectedIndex(index);
           }
       },
       {
           keys: ['k'],
           proceedToMainCallback: false,
           success: function(event) {
           var index = getTabSelectedIndex();
           ++index;
           if (index >= getTabsLength())
               index = 0;
           setTabsSelectedIndex(index);
           }
       }
   ], function(event){});
