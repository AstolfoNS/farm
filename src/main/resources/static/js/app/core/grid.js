(function (window, $) {
    function asNumber(value, def) {
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function listFromPageData(data) {
        if (!data) {
            return [];
        }
        if ($.isArray(data.records)) {
            return data.records;
        }
        if ($.isArray(data.rows)) {
            return data.rows;
        }
        return [];
    }

    function defaultResolve(res) {
        var ok = window.FarmApi && $.isFunction(window.FarmApi.isOk) && window.FarmApi.isOk(res);
        if (!ok || !res || !res.data) {
            return {rows: [], total: 0};
        }
        var rows = listFromPageData(res.data);
        return {
            rows: rows,
            total: asNumber(res.data.total, rows.length)
        };
    }

    function init(selector, options) {
        var defaults = {
            fit: true,
            fitColumns: true,
            striped: true,
            rownumbers: true,
            singleSelect: true,
            idField: "id"
        };
        return $(selector).datagrid($.extend(true, {}, defaults, options || {}));
    }

    function buildRemoteLoader(options) {
        var settings = $.extend({
            request: null,
            resolve: defaultResolve,
            onRows: null
        }, options || {});

        return function (param, success, error) {
            if (!$.isFunction(settings.request)) {
                success({total: 0, rows: []});
                return;
            }
            settings.request(param, function (res) {
                var resolved = $.isFunction(settings.resolve) ? settings.resolve(res, param) : defaultResolve(res);
                var rows = $.isArray(resolved && resolved.rows) ? resolved.rows : [];
                var total = asNumber(resolved && resolved.total, rows.length);
                if ($.isFunction(settings.onRows)) {
                    settings.onRows(rows, res, param);
                }
                success({total: total, rows: rows});
            }, function () {
                if ($.isFunction(error)) {
                    error.apply(this, arguments);
                }
            });
        };
    }

    function reload(selector, toFirstPage) {
        if (toFirstPage) {
            $(selector).datagrid("load", {page: 1});
            return;
        }
        $(selector).datagrid("reload");
    }

    function bindAction(containerSelector, itemSelector, handler) {
        $(containerSelector).off("click.farmGridAction", itemSelector).on("click.farmGridAction", itemSelector, function () {
            if ($.isFunction(handler)) {
                handler($(this));
            }
        });
    }

    window.FarmGrid = {
        asNumber: asNumber,
        listFromPageData: listFromPageData,
        defaultResolve: defaultResolve,
        init: init,
        buildRemoteLoader: buildRemoteLoader,
        reload: reload,
        bindAction: bindAction
    };
})(window, window.jQuery);
