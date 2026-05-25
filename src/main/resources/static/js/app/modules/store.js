(function (window, $) {
    var FarmStoreModule = {};
    var state = {
        active: false,
        inited: false
    };

    function asNumber(value, def) {
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function currentUserId() {
        if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.currentUserId)) {
            return asNumber(window.FarmHomeBridge.currentUserId(), 0);
        }
        return 0;
    }

    function escapeHtml(text) {
        return $("<div/>").text(text == null ? "" : String(text)).html();
    }

    function toArray(list) {
        return $.isArray(list) ? list : [];
    }

    function renderSeedList(seeds) {
        var list = toArray(seeds);
        if (list.length === 0) {
            $("#storeSeedList").html("<div class='store-empty'>暂无种子库存</div>");
            return;
        }
        var html = [];
        $.each(list, function (_, item) {
            html.push(
                "<div class='store-item'>" +
                "<div class='store-item-name'>" + escapeHtml(item.seedTypeName || ("种子#" + asNumber(item.seedTypeId, 0))) + "</div>" +
                "<div class='store-item-meta'>可用: " + asNumber(item.availableQuantity, 0) + " | 冻结: " + asNumber(item.frozenQuantity, 0) + "</div>" +
                "</div>"
            );
        });
        $("#storeSeedList").html(html.join(""));
    }

    function renderPlotSummary(data) {
        var freeCount = asNumber(data && data.freePlantablePlotCount, 0);
        var allCount = asNumber(data && data.allPlantablePlotCount, 0);
        var selectable = data && data.hasSelectableSeed ? "是" : "否";
        $("#storePlotSummary").html(
            "<div class='store-item'>" +
            "<div class='store-item-name'>可用地块: " + freeCount + " / " + allCount + "</div>" +
            "<div class='store-item-meta'>当前是否有可种种子: " + selectable + "</div>" +
            "</div>"
        );
    }

    function renderFruitList(records) {
        var list = toArray(records);
        if (list.length === 0) {
            $("#storeFruitList").html("<div class='store-empty'>暂无果实库存</div>");
            return;
        }
        var html = [];
        $.each(list, function (_, item) {
            html.push(
                "<div class='store-item'>" +
                "<div class='store-item-name'>" + escapeHtml(item.seedName || ("果实#" + asNumber(item.seedTypeId, 0))) + "</div>" +
                "<div class='store-item-meta'>可售: " + asNumber(item.availableQuantity, 0) + " | 冻结: " + asNumber(item.frozenQuantity, 0) + "</div>" +
                "<div class='store-item-meta'>单价: " + asNumber(item.unitFruitPrice, 0) + " | 估值: " + asNumber(item.estimatedIncomeCoin, 0) + "</div>" +
                "</div>"
            );
        });
        $("#storeFruitList").html(html.join(""));
    }

    function renderNoUser() {
        $("#storeSeedList").html("<div class='store-empty'>请先在“用户选择”中选择用户</div>");
        $("#storeFruitList").html("<div class='store-empty'>请先在“用户选择”中选择用户</div>");
        $("#storePlotSummary").html("<div class='store-empty'>请先在“用户选择”中选择用户</div>");
    }

    function reload() {
        if (!state.active) {
            return;
        }
        var uid = currentUserId();
        if (uid <= 0) {
            renderNoUser();
            return;
        }

        FarmApi.myPlantingPanel(uid, function (res) {
            if (!(FarmApi.isOk(res) && res.data)) {
                $("#storeSeedList").html("<div class='store-empty'>读取种子库存失败</div>");
                $("#storePlotSummary").html("<div class='store-empty'>读取地块摘要失败</div>");
                return;
            }
            renderSeedList(res.data.seeds);
            renderPlotSummary(res.data);
        }, function () {
            $("#storeSeedList").html("<div class='store-empty'>读取种子库存失败</div>");
            $("#storePlotSummary").html("<div class='store-empty'>读取地块摘要失败</div>");
        });

        FarmApi.fruitPage({userId: uid, page: 1, rows: 10}, function (res) {
            if (!(FarmApi.isOk(res) && res.data)) {
                $("#storeFruitList").html("<div class='store-empty'>读取果实库存失败</div>");
                return;
            }
            renderFruitList(res.data.records);
        }, function () {
            $("#storeFruitList").html("<div class='store-empty'>读取果实库存失败</div>");
        });
    }

    function bindEvents() {
        $("#storeRefreshBtn").on("click", function () {
            reload();
        });
    }

    function ensureInit() {
        if (state.inited) {
            return;
        }
        $("#storeRefreshBtn").linkbutton();
        bindEvents();
        state.inited = true;
    }

    function setActive(flag) {
        state.active = !!flag;
        if (state.active) {
            ensureInit();
            $("#storePanel").addClass("is-active");
            reload();
            return;
        }
        $("#storePanel").removeClass("is-active");
    }

    FarmStoreModule.setActive = setActive;
    FarmStoreModule.reload = reload;
    window.FarmStoreModule = FarmStoreModule;
})(window, window.jQuery);
