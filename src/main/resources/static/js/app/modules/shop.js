(function (window, $) {
    var FarmShopModule = {};
    var state = {
        active: false,
        inited: false,
        userId: 0,
        loading: false,
        currentTab: "seed",
        seedQuery: {
            page: 1,
            rows: 8,
            sort: "id",
            order: "asc",
            name: "",
            seedQualityId: null,
            level: null
        },
        fruitQuery: {
            page: 1,
            rows: 10
        },
        tradeQuery: {
            page: 1,
            rows: 10
        }
    };

    function motion() {
        if ($.isFunction(window.farmMotion)) {
            return window.farmMotion();
        }
        return {moduleEnterMs: 260, pageSwitchMs: 220, actionFeedbackMs: 1200};
    }

    function asNumber(value, def) {
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function trimText(value) {
        return $.trim(value == null ? "" : String(value));
    }

    function currentUserId() {
        if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.currentUserId)) {
            return asNumber(window.FarmHomeBridge.currentUserId(), 0);
        }
        return 0;
    }

    function playSound(key) {
        if (window.FarmAudio && $.isFunction(window.FarmAudio.play)) {
            window.FarmAudio.play(key);
        }
    }

    function beginPageTransition() {
        $("#shopSeedList, #shopFruitList, #shopTradeList").addClass("is-page-switching");
    }

    function endPageTransition() {
        window.setTimeout(function () {
            $("#shopSeedList, #shopFruitList, #shopTradeList").removeClass("is-page-switching");
        }, motion().pageSwitchMs);
    }

    function escapeHtml(text) {
        return $("<div/>").text(text == null ? "" : String(text)).html();
    }

    function escapeAttr(text) {
        var value = text == null ? "" : String(text);
        return value
            .replace(/&/g, "&amp;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;");
    }

    function toArray(list) {
        return $.isArray(list) ? list : [];
    }

    function renderOverview(overview) {
        var data = overview || {};
        $("#shopCurrentCoin").text(asNumber(data.currentCoin, 0));
        $("#shopSellableValue").text(asNumber(data.sellableTotalValue, 0));
        $("#shopSellableFruitCount").text(asNumber(data.sellableFruitTotalCount, 0));
        $("#shopPurchasableTypeCount").text(asNumber(data.purchasableSeedTypeCount, 0));
    }

    function switchTab(tabName) {
        state.currentTab = tabName;
        $(".shop-tab").removeClass("is-active");
        $(".shop-tab[data-tab='" + tabName + "']").addClass("is-active");
        $(".shop-pane").removeClass("is-active");
        if (tabName === "fruit") {
            $("#shopFruitPane").addClass("is-active");
            return;
        }
        if (tabName === "trade") {
            $("#shopTradePane").addClass("is-active");
            return;
        }
        $("#shopSeedPane").addClass("is-active");
    }

    function buildLevelOptions() {
        var options = [{id: "", text: "全部等级"}];
        var i;
        for (i = 1; i <= 10; i = i + 1) {
            options.push({id: i, text: "等级 " + i});
        }
        return options;
    }

    function loadQualityOptions() {
        FarmApi.listSeedQualityOptions(function (res) {
            var data = [{id: "", text: "全部品质"}];
            if (FarmApi.isOk(res) && $.isArray(res.data)) {
                $.each(res.data, function (_, item) {
                    data.push({
                        id: asNumber(item.id, 0),
                        text: item.text || ("品质#" + asNumber(item.id, 0))
                    });
                });
            }
            $("#shopSeedQuality").combobox({
                valueField: "id",
                textField: "text",
                editable: false,
                panelHeight: 180,
                data: data
            });
            $("#shopSeedQuality").combobox("setValue", "");
        }, function () {
            $("#shopSeedQuality").combobox({
                valueField: "id",
                textField: "text",
                editable: false,
                panelHeight: 80,
                data: [{id: "", text: "全部品质"}]
            });
            $("#shopSeedQuality").combobox("setValue", "");
        });
    }

    function initFilterBar() {
        $("#shopSeedName").textbox({
            prompt: "按名称筛�?
        });
        $("#shopSeedLevel").combobox({
            valueField: "id",
            textField: "text",
            editable: false,
            panelHeight: 180,
            data: buildLevelOptions()
        });
        $("#shopSeedLevel").combobox("setValue", "");
        loadQualityOptions();
        $("#shopSeedFilterBtn").linkbutton();
        $("#shopSeedResetBtn").linkbutton();
        bindFilterEvents();
    }

    function collectSeedFilters() {
        var name = trimText($("#shopSeedName").textbox("getValue"));
        var qualityId = asNumber($("#shopSeedQuality").combobox("getValue"), 0);
        var level = asNumber($("#shopSeedLevel").combobox("getValue"), 0);
        state.seedQuery.name = name;
        state.seedQuery.seedQualityId = qualityId > 0 ? qualityId : null;
        state.seedQuery.level = level > 0 ? level : null;
    }

    function applySeedFilter() {
        collectSeedFilters();
        state.seedQuery.page = 1;
        playSound("click");
        beginPageTransition();
        reload();
    }

    function resetSeedFilter() {
        $("#shopSeedName").textbox("setValue", "");
        $("#shopSeedQuality").combobox("setValue", "");
        $("#shopSeedLevel").combobox("setValue", "");
        state.seedQuery.name = "";
        state.seedQuery.seedQualityId = null;
        state.seedQuery.level = null;
        state.seedQuery.page = 1;
        playSound("click");
        beginPageTransition();
        reload();
    }

    function bindFilterEvents() {
        $("#shopSeedFilterBtn").off("click").on("click", function () {
            applySeedFilter();
        });
        $("#shopSeedResetBtn").off("click").on("click", function () {
            resetSeedFilter();
        });
        $("#shopSeedName").textbox("textbox").off("keydown").on("keydown", function (event) {
            if (event && event.keyCode === 13) {
                applySeedFilter();
            }
        });
    }

    function totalPages(pageResult) {
        var total = asNumber(pageResult && pageResult.total, 0);
        var pageSize = asNumber(pageResult && pageResult.pageSize, 10);
        if (pageSize <= 0) {
            pageSize = 10;
        }
        var pages = Math.ceil(total / pageSize);
        return pages <= 0 ? 1 : pages;
    }

    function resolveCover(url) {
        if (url && $.trim(url).length > 0) {
            return url;
        }
        return farmResolveImg("app/user/default-avatar.png");
    }

    function renderSeedList(pageData) {
        var records = toArray(pageData && pageData.records);
        if (records.length === 0) {
            $("#shopSeedList").html("<div class='shop-empty'>暂无可购买种�?/div>");
            $("#shopSeedPager").empty();
            return;
        }
        var html = [];
        $.each(records, function (_, row) {
            var seedTypeId = asNumber(row.id, 0);
            var price = asNumber(row.price, 0);
            html.push(
                "<div class='shop-seed-card'>" +
                "<div class='shop-seed-name'>" + escapeHtml(row.name || ("种子#" + seedTypeId)) + "</div>" +
                "<div class='shop-seed-meta'>品质: " + escapeHtml(row.seedQualityName || "-") + " | 等级: " + asNumber(row.level, 0) + "</div>" +
                "<div class='shop-seed-meta'>土地需�? " + escapeHtml(row.enableSoilTypeNames || "-") + "</div>" +
                "<div class='shop-seed-desc'>" + escapeHtml(row.description || "暂无描述") + "</div>" +
                "<img class='shop-seed-cover' src='" + escapeAttr(resolveCover(row.coverImageUrl)) + "' alt=''>" +
                "<div class='shop-seed-price'>采购�? " + price + " 金币 | 预估净�? " + asNumber(row.estimatedNetValue, 0) + "</div>" +
                "<div class='shop-seed-actions'>" +
                "<a href='javascript:void(0)' class='easyui-linkbutton c1 shop-buy-btn' data-seed-type-id='" + seedTypeId + "' data-price='" + price + "' data-seed-name='" + escapeAttr(row.name || "") + "'>我要购买</a>" +
                "</div>" +
                "</div>"
            );
        });
        $("#shopSeedList").html(html.join(""));
        $("#shopSeedList .easyui-linkbutton").linkbutton();
        bindSeedActions();
        renderSeedPager(pageData);
    }

    function renderSeedPager(pageData) {
        var pageNo = asNumber(pageData && pageData.pageNo, 1);
        var pages = totalPages(pageData || {});
        var prevDisabled = pageNo <= 1 ? "disabled" : "";
        var nextDisabled = pageNo >= pages ? "disabled" : "";
        var html = "" +
            "<button type='button' class='shop-page-btn prev shop-page-prev' " + prevDisabled + "></button>" +
            "<span class='shop-page-label'>�?" + pageNo + " / " + pages + " �?/span>" +
            "<button type='button' class='shop-page-btn next shop-page-next' " + nextDisabled + "></button>";
        $("#shopSeedPager").html(html);
        $("#shopSeedPager .shop-page-prev").off("click").on("click", function () {
            if (state.seedQuery.page <= 1) {
                return;
            }
            state.seedQuery.page = state.seedQuery.page - 1;
            beginPageTransition();
            reload();
        });
        $("#shopSeedPager .shop-page-next").off("click").on("click", function () {
            if (state.seedQuery.page >= pages) {
                return;
            }
            state.seedQuery.page = state.seedQuery.page + 1;
            beginPageTransition();
            reload();
        });
    }

    function renderFruitList(pageData) {
        var records = toArray(pageData && pageData.records);
        if (records.length === 0) {
            $("#shopFruitList").html("<div class='shop-empty'>暂无果实库存</div>");
            return;
        }
        var html = [];
        $.each(records, function (_, row) {
            var seedTypeId = asNumber(row.seedTypeId, 0);
            var available = asNumber(row.availableQuantity, 0);
            var unitPrice = asNumber(row.unitFruitPrice, 0);
            html.push(
                "<div class='shop-fruit-item'>" +
                "<div class='shop-fruit-name'>" + escapeHtml(row.seedName || ("果实#" + seedTypeId)) + "</div>" +
                "<div class='shop-fruit-meta'>可售: " + available + " | 冻结: " + asNumber(row.frozenQuantity, 0) + " | 单价: " + unitPrice + "</div>" +
                "<div class='shop-fruit-meta'>预计可售: " + asNumber(row.estimatedIncomeCoin, 0) + " 金币</div>" +
                "<div class='shop-seed-actions'>" +
                "<a href='javascript:void(0)' class='easyui-linkbutton shop-sell-btn' data-seed-type-id='" + seedTypeId + "' data-seed-name='" + escapeAttr(row.seedName || "") + "' data-available='" + available + "' data-unit-price='" + unitPrice + "' " + (available <= 0 ? "disabled='disabled'" : "") + ">出售</a>" +
                "</div>" +
                "</div>"
            );
        });
        $("#shopFruitList").html(html.join(""));
        $("#shopFruitList .easyui-linkbutton").linkbutton();
        bindFruitActions();
    }

    function renderTradeList(pageData) {
        var records = toArray(pageData && pageData.records);
        if (records.length === 0) {
            $("#shopTradeList").html("<div class='shop-empty'>暂无交易记录</div>");
            return;
        }
        var html = [];
        $.each(records, function (_, row) {
            var tradeType = row.tradeType === "SELL" ? "出售" : "购买";
            var typeCss = row.tradeType === "SELL" ? "SELL" : "BUY";
            var amount = asNumber(row.coinChangeAmount, 0);
            var sign = row.coinOperationType === "INCOME" ? "+" : "-";
            html.push(
                "<div class='shop-trade-item'>" +
                "<div class='shop-fruit-name'><span class='shop-trade-type'>" + typeCss + "</span> " + escapeHtml(row.seedName || ("种子#" + asNumber(row.seedTypeId, 0))) + "</div>" +
                "<div class='shop-trade-meta'>" + tradeType + "数量: " + asNumber(row.itemQuantity, 0) + " | 金币变化: " + sign + amount + "</div>" +
                "<div class='shop-trade-meta'>时间: " + escapeHtml(row.occurredAt || "-") + "</div>" +
                "</div>"
            );
        });
        $("#shopTradeList").html(html.join(""));
    }

    function ensureBuyDialog() {
        if ($("#shopBuyDialog").length > 0) {
            return;
        }
        $("body").append(
            "<div id='shopBuyDialog' style='display:none;'>" +
            "<div class='farm-action-row' id='shopBuySeedLabel'>种子</div>" +
            "<div class='farm-action-row'>数量: <input id='shopBuyQty' style='width:180px;'></div>" +
            "<div class='farm-action-row' id='shopBuyCostTip'>预计花费: 0 金币</div>" +
            "</div>"
        );
        $("#shopBuyDialog").dialog({
            width: 380,
            height: 230,
            modal: true,
            closed: true,
            buttons: [{
                text: "确认购买",
                handler: function () {
                    submitBuy();
                }
            }, {
                text: "取消",
                handler: function () {
                    $("#shopBuyDialog").dialog("close");
                }
            }]
        });
        $("#shopBuyQty").numberbox({
            min: 1,
            precision: 0,
            value: 1
        });
    }

    function ensureSellDialog() {
        if ($("#shopSellDialog").length > 0) {
            return;
        }
        $("body").append(
            "<div id='shopSellDialog' style='display:none;'>" +
            "<div class='farm-action-row' id='shopSellSeedLabel'>果实</div>" +
            "<div class='farm-action-row'>数量: <input id='shopSellQty' style='width:180px;'></div>" +
            "<div class='farm-action-row' id='shopSellIncomeTip'>预计获得: 0 金币</div>" +
            "</div>"
        );
        $("#shopSellDialog").dialog({
            width: 380,
            height: 230,
            modal: true,
            closed: true,
            buttons: [{
                text: "确认出售",
                handler: function () {
                    submitSell();
                }
            }, {
                text: "取消",
                handler: function () {
                    $("#shopSellDialog").dialog("close");
                }
            }]
        });
        $("#shopSellQty").numberbox({
            min: 1,
            precision: 0,
            value: 1
        });
    }

    function openBuyDialog(seedTypeId, seedName, price) {
        ensureBuyDialog();
        $("#shopBuyDialog").data("seedTypeId", seedTypeId);
        $("#shopBuyDialog").data("unitPrice", price);
        $("#shopBuySeedLabel").text("种子: " + (seedName || ("种子#" + seedTypeId)));
        $("#shopBuyQty").numberbox("setValue", 1);
        $("#shopBuyCostTip").text("预计花费: " + asNumber(price, 0) + " 金币");
        $("#shopBuyQty").numberbox({
            onChange: function (newVal) {
                var qty = asNumber(newVal, 1);
                if (qty <= 0) {
                    qty = 1;
                }
                $("#shopBuyCostTip").text("预计花费: " + (qty * asNumber(price, 0)) + " 金币");
            }
        });
        $("#shopBuyDialog").dialog("setTitle", "购买种子").dialog("open");
        playSound("open");
    }

    function openSellDialog(seedTypeId, seedName, available, unitPrice) {
        ensureSellDialog();
        $("#shopSellDialog").data("seedTypeId", seedTypeId);
        $("#shopSellDialog").data("unitPrice", unitPrice);
        $("#shopSellDialog").data("maxQty", available);
        $("#shopSellSeedLabel").text("果实: " + (seedName || ("种子#" + seedTypeId)) + " (最�?" + available + ")");
        $("#shopSellQty").numberbox("setValue", 1);
        $("#shopSellQty").numberbox({min: 1, max: available});
        $("#shopSellIncomeTip").text("预计获得: " + asNumber(unitPrice, 0) + " 金币");
        $("#shopSellQty").numberbox({
            onChange: function (newVal) {
                var qty = asNumber(newVal, 1);
                if (qty <= 0) {
                    qty = 1;
                }
                $("#shopSellIncomeTip").text("预计获得: " + (qty * asNumber(unitPrice, 0)) + " 金币");
            }
        });
        $("#shopSellDialog").dialog("setTitle", "出售果实").dialog("open");
        playSound("open");
    }

    function submitBuy() {
        var uid = currentUserId();
        var seedTypeId = asNumber($("#shopBuyDialog").data("seedTypeId"), 0);
        var quantity = asNumber($("#shopBuyQty").numberbox("getValue"), 0);
        if (uid <= 0 || seedTypeId <= 0 || quantity <= 0) {
            $.messager.alert("提示", "购买参数无效");
            playSound("error");
            return;
        }
        FarmApi.shopBuy({
            requestId: farmBuildRequestId("buy_seed"),
            userId: uid,
            seedTypeId: seedTypeId,
            quantity: quantity
        }, function (res) {
            if (!FarmApi.isOk(res)) {
                $.messager.alert("提示", (res && res.msg) ? res.msg : "购买失败");
                playSound("error");
                return;
            }
            $("#shopBuyDialog").dialog("close");
            $.messager.show({title: "提示", msg: "购买成功", timeout: motion().actionFeedbackMs, showType: "slide"});
            playSound("buy");
            if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.refreshCurUser)) {
                window.FarmHomeBridge.refreshCurUser();
            }
            beginPageTransition();
            reload();
        }, function () {
            $.messager.alert("提示", "购买失败，请稍后重试");
            playSound("error");
        });
    }

    function submitSell() {
        var uid = currentUserId();
        var seedTypeId = asNumber($("#shopSellDialog").data("seedTypeId"), 0);
        var quantity = asNumber($("#shopSellQty").numberbox("getValue"), 0);
        var maxQty = asNumber($("#shopSellDialog").data("maxQty"), 0);
        if (uid <= 0 || seedTypeId <= 0 || quantity <= 0 || quantity > maxQty) {
            $.messager.alert("提示", "出售参数无效");
            playSound("error");
            return;
        }
        FarmApi.sellFruit({
            requestId: farmBuildRequestId("sell_fruit"),
            userId: uid,
            seedTypeId: seedTypeId,
            quantity: quantity
        }, function (res) {
            if (!FarmApi.isOk(res)) {
                $.messager.alert("提示", (res && res.msg) ? res.msg : "出售失败");
                playSound("error");
                return;
            }
            $("#shopSellDialog").dialog("close");
            $.messager.show({title: "提示", msg: "出售成功", timeout: motion().actionFeedbackMs, showType: "slide"});
            playSound("sell");
            if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.refreshCurUser)) {
                window.FarmHomeBridge.refreshCurUser();
            }
            beginPageTransition();
            reload();
        }, function () {
            $.messager.alert("提示", "出售失败，请稍后重试");
            playSound("error");
        });
    }

    function bindSeedActions() {
        $("#shopSeedList .shop-buy-btn").off("click").on("click", function () {
            var $btn = $(this);
            var seedTypeId = asNumber($btn.attr("data-seed-type-id"), 0);
            var seedName = $btn.attr("data-seed-name") || "";
            var price = asNumber($btn.attr("data-price"), 0);
            openBuyDialog(seedTypeId, seedName, price);
        });
    }

    function bindFruitActions() {
        $("#shopFruitList .shop-sell-btn").off("click").on("click", function () {
            var $btn = $(this);
            if ($btn.linkbutton("options").disabled) {
                return;
            }
            var seedTypeId = asNumber($btn.attr("data-seed-type-id"), 0);
            var seedName = $btn.attr("data-seed-name") || "";
            var available = asNumber($btn.attr("data-available"), 0);
            var unitPrice = asNumber($btn.attr("data-unit-price"), 0);
            openSellDialog(seedTypeId, seedName, available, unitPrice);
        });
    }

    function loadFruitPage() {
        if (!state.active || state.userId <= 0) {
            return;
        }
        FarmApi.fruitPage({
            userId: state.userId,
            page: state.fruitQuery.page,
            rows: state.fruitQuery.rows
        }, function (res) {
            if (!FarmApi.isOk(res)) {
                return;
            }
            renderFruitList(res.data);
        });
    }

    function loadTradePage() {
        if (!state.active || state.userId <= 0) {
            return;
        }
        FarmApi.shopTradePage({
            userId: state.userId,
            page: state.tradeQuery.page,
            rows: state.tradeQuery.rows
        }, function (res) {
            if (!FarmApi.isOk(res)) {
                return;
            }
            renderTradeList(res.data);
        });
    }

    function renderNoUser() {
        renderOverview({});
        $("#shopSeedList").html("<div class='shop-empty'>请先在首页选择用户</div>");
        $("#shopSeedPager").empty();
        $("#shopFruitList").html("<div class='shop-empty'>请先在首页选择用户</div>");
        $("#shopTradeList").html("<div class='shop-empty'>请先在首页选择用户</div>");
    }

    function reload() {
        if (!state.active || state.loading) {
            return;
        }
        beginPageTransition();
        state.userId = currentUserId();
        if (state.userId <= 0) {
            renderNoUser();
            endPageTransition();
            return;
        }
        state.loading = true;
        FarmApi.shopHome({
            userId: state.userId,
            page: state.seedQuery.page,
            rows: state.seedQuery.rows,
            sort: state.seedQuery.sort,
            order: state.seedQuery.order,
            name: state.seedQuery.name,
            seedQualityId: state.seedQuery.seedQualityId,
            level: state.seedQuery.level
        }, function (res) {
            state.loading = false;
            if (!(FarmApi.isOk(res) && res.data)) {
                $.messager.alert("提示", (res && res.msg) ? res.msg : "加载商店失败");
                playSound("error");
                endPageTransition();
                return;
            }
            renderOverview(res.data.overview);
            renderSeedList(res.data.shopPage);
            loadFruitPage();
            loadTradePage();
            endPageTransition();
        }, function () {
            state.loading = false;
            $.messager.alert("提示", "加载商店失败，请稍后重试");
            playSound("error");
            endPageTransition();
        });
    }

    function bindTabs() {
        $(".shop-tab").off("click").on("click", function () {
            switchTab(String($(this).attr("data-tab") || "seed"));
            playSound("click");
        });
    }

    function setActive(flag) {
        state.active = !!flag;
        if (state.active) {
            if (!state.inited) {
                initFilterBar();
                bindTabs();
                state.inited = true;
            }
            $("#shopPanel").addClass("is-active is-module-enter");
            window.setTimeout(function () {
                $("#shopPanel").removeClass("is-module-enter");
            }, motion().moduleEnterMs);
            switchTab(state.currentTab || "seed");
            reload();
            return;
        }
        $("#shopPanel").removeClass("is-active");
    }

    FarmShopModule.setActive = setActive;
    FarmShopModule.reload = reload;

    window.FarmShopModule = FarmShopModule;
    if (window.FarmCore && $.isFunction(window.FarmCore.registerSetActiveModule)) {
        window.FarmCore.registerSetActiveModule("shop", FarmShopModule, {refreshMethod: "reload"});
    }
})(window, window.jQuery);

