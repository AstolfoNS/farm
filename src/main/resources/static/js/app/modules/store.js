(function (window, $) {
    var FarmStoreModule = {};
    var state = {
        active: false,
        inited: false,
        seedQuery: {
            page: 1,
            rows: 8
        },
        fruitQuery: {
            page: 1,
            rows: 8
        }
    };

    function motion() {
        if ($.isFunction(window.farmMotion)) {
            return window.farmMotion();
        }
        return {actionFeedbackMs: 1200};
    }

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

    function resolveCover(url) {
        if (url && $.trim(url).length > 0) {
            return url;
        }
        return (window.farmDefaultAsset && window.farmDefaultAsset("seedCover")) || "/oss/defaults/seed/seed-cover-default.png";
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

    function renderOverview(seedPage, fruitPage) {
        var seedRows = toArray(seedPage && seedPage.records);
        var fruitRows = toArray(fruitPage && fruitPage.records);
        var seedTotal = 0;
        var fruitTotal = 0;
        var sellableFruit = 0;
        var sellableValue = 0;
        $.each(seedRows, function (_, item) {
            seedTotal += asNumber(item.quantity, 0);
        });
        $.each(fruitRows, function (_, item) {
            var available = asNumber(item.availableQuantity, 0);
            var price = asNumber(item.unitFruitPrice, 0);
            fruitTotal += asNumber(item.fruitQuantity, 0);
            sellableFruit += available;
            sellableValue += (available * price);
        });
        $("#storeSeedTotal").text(seedTotal);
        $("#storeFruitTotal").text(fruitTotal);
        $("#storeSellableFruit").text(sellableFruit);
        $("#storeSellableValue").text(sellableValue);
    }

    function renderSeedList(pageData) {
        var records = toArray(pageData && pageData.records);
        if (records.length === 0) {
            $("#storeSeedList").html("<div class='store-empty'>暂无种子库存</div>");
            $("#storeSeedPager").empty();
            return;
        }
        var html = [];
        $.each(records, function (_, item) {
            html.push(
                "<div class='store-item'>" +
                "<div class='store-item-head'>" +
                "<img class='store-item-cover' src='" + escapeAttr(resolveCover(item.coverImageUrl)) + "' alt=''>" +
                "<div class='store-item-title-wrap'>" +
                "<div class='store-item-name'>" + escapeHtml(item.seedName || ("种子#" + asNumber(item.seedTypeId, 0))) + "</div>" +
                "<div class='store-item-meta'>采购价: " + asNumber(item.unitBuyPrice, 0) + " 金币 | 解锁经验: " + asNumber(item.unlockExperienceRequired, 0) + "</div>" +
                "</div>" +
                "</div>" +
                "<div class='store-item-meta'>总量: " + asNumber(item.quantity, 0) + " | 冻结: " + asNumber(item.frozenQuantity, 0) + " | 可用: " + asNumber(item.availableQuantity, 0) + "</div>" +
                "</div>"
            );
        });
        $("#storeSeedList").html(html.join(""));
        renderSeedPager(pageData);
    }

    function renderFruitList(pageData) {
        var records = toArray(pageData && pageData.records);
        if (records.length === 0) {
            $("#storeFruitList").html("<div class='store-empty'>暂无果实库存</div>");
            $("#storeFruitPager").empty();
            return;
        }
        var html = [];
        $.each(records, function (_, item) {
            var available = asNumber(item.availableQuantity, 0);
            var disabled = available <= 0;
            var btnText = disabled ? "暂无可售" : "出售果实";
            html.push(
                "<div class='store-item'>" +
                "<div class='store-item-head'>" +
                "<img class='store-item-cover' src='" + escapeAttr(resolveCover(item.coverImageUrl)) + "' alt=''>" +
                "<div class='store-item-title-wrap'>" +
                "<div class='store-item-name'>" + escapeHtml(item.seedName || ("果实#" + asNumber(item.seedTypeId, 0))) + "</div>" +
                "<div class='store-item-meta'>单价: " + asNumber(item.unitFruitPrice, 0) + " 金币 | 可售估值: " + asNumber(item.estimatedIncomeCoin, 0) + "</div>" +
                "</div>" +
                "</div>" +
                "<div class='store-item-meta'>总量: " + asNumber(item.fruitQuantity, 0) + " | 冻结: " + asNumber(item.frozenQuantity, 0) + " | 可售: " + available + "</div>" +
                "<div class='store-item-actions'>" +
                "<a href='javascript:void(0)' class='easyui-linkbutton " + (disabled ? "c5" : "c1") + " store-sell-btn' data-seed-type-id='" + asNumber(item.seedTypeId, 0) + "' data-seed-name='" + escapeAttr(item.seedName || "") + "' data-max-qty='" + available + "' data-unit-price='" + asNumber(item.unitFruitPrice, 0) + "' " + (disabled ? "disabled='disabled'" : "") + ">" + btnText + "</a>" +
                "</div>" +
                "</div>"
            );
        });
        $("#storeFruitList").html(html.join(""));
        $("#storeFruitList .easyui-linkbutton").linkbutton();
        bindSellEvents();
        renderFruitPager(pageData);
    }

    function renderSeedPager(pageData) {
        var pageNo = asNumber(pageData && pageData.pageNo, 1);
        var pages = totalPages(pageData || {});
        var prevDisabled = pageNo <= 1 ? "disabled" : "";
        var nextDisabled = pageNo >= pages ? "disabled" : "";
        var html = "" +
            "<button type='button' class='store-page-btn prev store-seed-page-prev' " + prevDisabled + "></button>" +
            "<span class='store-page-label'>第 " + pageNo + " / " + pages + " 页</span>" +
            "<button type='button' class='store-page-btn next store-seed-page-next' " + nextDisabled + "></button>";
        $("#storeSeedPager").html(html);
        $("#storeSeedPager .store-seed-page-prev").off("click.storeSeedPager").on("click.storeSeedPager", function () {
            if (state.seedQuery.page <= 1) {
                return;
            }
            state.seedQuery.page = state.seedQuery.page - 1;
            reload();
        });
        $("#storeSeedPager .store-seed-page-next").off("click.storeSeedPager").on("click.storeSeedPager", function () {
            if (state.seedQuery.page >= pages) {
                return;
            }
            state.seedQuery.page = state.seedQuery.page + 1;
            reload();
        });
    }

    function renderFruitPager(pageData) {
        var pageNo = asNumber(pageData && pageData.pageNo, 1);
        var pages = totalPages(pageData || {});
        var prevDisabled = pageNo <= 1 ? "disabled" : "";
        var nextDisabled = pageNo >= pages ? "disabled" : "";
        var html = "" +
            "<button type='button' class='store-page-btn prev store-fruit-page-prev' " + prevDisabled + "></button>" +
            "<span class='store-page-label'>第 " + pageNo + " / " + pages + " 页</span>" +
            "<button type='button' class='store-page-btn next store-fruit-page-next' " + nextDisabled + "></button>";
        $("#storeFruitPager").html(html);
        $("#storeFruitPager .store-fruit-page-prev").off("click.storeFruitPager").on("click.storeFruitPager", function () {
            if (state.fruitQuery.page <= 1) {
                return;
            }
            state.fruitQuery.page = state.fruitQuery.page - 1;
            reload();
        });
        $("#storeFruitPager .store-fruit-page-next").off("click.storeFruitPager").on("click.storeFruitPager", function () {
            if (state.fruitQuery.page >= pages) {
                return;
            }
            state.fruitQuery.page = state.fruitQuery.page + 1;
            reload();
        });
    }

    function renderNoUser() {
        $("#storeSeedList").html("<div class='store-empty'>请先在“用户选择”中选择用户</div>");
        $("#storeFruitList").html("<div class='store-empty'>请先在“用户选择”中选择用户</div>");
        $("#storeSeedPager").empty();
        $("#storeFruitPager").empty();
        $("#storeSeedTotal, #storeFruitTotal, #storeSellableFruit, #storeSellableValue").text("0");
    }

    function ensureSellDialog() {
        if ($("#storeSellDialog").length > 0) {
            return;
        }
        $("body").append(
            "<div id='storeSellDialog' class='store-action-dialog' style='display:none;'>" +
            "<div class='farm-dialog-shell store-action-dialog-shell'>" +
            "<div class='farm-action-row' id='storeSellSeedLabel'>果实</div>" +
            "<div class='farm-action-row'>数量: <input id='storeSellQty' style='width:180px;'></div>" +
            "<div class='farm-action-row' id='storeSellIncomeTip'>预计获得: 0 金币</div>" +
            "</div>" +
            "</div>"
        );
        $("#storeSellDialog").dialog({
            width: 380,
            height: 230,
            modal: true,
            closed: true,
            cls: "farm-dialog-window store-dialog-window",
            onOpen: function () {
                var $panel = $(this).dialog("dialog");
                $panel.find(".dialog-button").addClass("farm-dialog-actions store-dialog-actions");
            },
            buttons: [{
                text: "确认出售",
                handler: function () {
                    submitSell();
                }
            }, {
                text: "取消",
                handler: function () {
                    $("#storeSellDialog").dialog("close");
                }
            }]
        });
        var $sellButtons = $("#storeSellDialog").dialog("dialog").find(".dialog-button .l-btn");
        $sellButtons.eq(0).addClass("c2");
        $sellButtons.eq(1).addClass("c5");
        $("#storeSellQty").numberbox({
            min: 1,
            precision: 0,
            value: 1
        });
    }

    function openSellDialog(seedTypeId, seedName, available, unitPrice) {
        ensureSellDialog();
        $("#storeSellDialog").data("seedTypeId", seedTypeId);
        $("#storeSellDialog").data("unitPrice", unitPrice);
        $("#storeSellDialog").data("maxQty", available);
        $("#storeSellSeedLabel").text("果实: " + (seedName || ("种子#" + seedTypeId)) + " (最多 " + available + ")");
        $("#storeSellQty").numberbox("setValue", 1);
        $("#storeSellQty").numberbox({min: 1, max: available});
        $("#storeSellIncomeTip").text("预计获得: " + asNumber(unitPrice, 0) + " 金币");
        $("#storeSellQty").numberbox({
            onChange: function (newVal) {
                var qty = asNumber(newVal, 1);
                if (qty <= 0) {
                    qty = 1;
                }
                $("#storeSellIncomeTip").text("预计获得: " + (qty * asNumber(unitPrice, 0)) + " 金币");
            }
        });
        $("#storeSellDialog").dialog("setTitle", "出售果实").dialog("open");
    }

    function submitSell() {
        var uid = currentUserId();
        var seedTypeId = asNumber($("#storeSellDialog").data("seedTypeId"), 0);
        var quantity = asNumber($("#storeSellQty").numberbox("getValue"), 0);
        var maxQty = asNumber($("#storeSellDialog").data("maxQty"), 0);
        if (uid <= 0 || seedTypeId <= 0 || quantity <= 0 || quantity > maxQty) {
            $.messager.alert("提示", "出售参数无效");
            return;
        }
        FarmApi.sellFruit({
            requestId: farmBuildRequestId("store_sell_fruit"),
            userId: uid,
            seedTypeId: seedTypeId,
            quantity: quantity
        }, function (res) {
            if (!FarmApi.isOk(res)) {
                $.messager.alert("提示", (res && res.msg) ? res.msg : "出售失败");
                return;
            }
            $("#storeSellDialog").dialog("close");
            $.messager.show({title: "提示", msg: "出售成功", timeout: motion().actionFeedbackMs, showType: "slide"});
            if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.refreshCurUser)) {
                window.FarmHomeBridge.refreshCurUser();
            }
            reload();
        }, function () {
            $.messager.alert("提示", "出售失败，请稍后重试");
        });
    }

    function bindSellEvents() {
        $("#storeFruitList .store-sell-btn").off("click.storeSell").on("click.storeSell", function () {
            var $btn = $(this);
            if ($btn.attr("disabled")) {
                return;
            }
            var seedTypeId = asNumber($btn.attr("data-seed-type-id"), 0);
            var seedName = $btn.attr("data-seed-name") || "";
            var maxQty = asNumber($btn.attr("data-max-qty"), 0);
            var unitPrice = asNumber($btn.attr("data-unit-price"), 0);
            if (seedTypeId <= 0 || maxQty <= 0) {
                $.messager.alert("提示", "当前果实不可出售");
                return;
            }
            openSellDialog(seedTypeId, seedName, maxQty, unitPrice);
        });
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
        var seedPageData = null;
        var fruitPageData = null;

        FarmApi.seedInventoryPage({
            userId: uid,
            page: state.seedQuery.page,
            rows: state.seedQuery.rows
        }, function (res) {
            if (FarmApi.isOk(res) && res.data) {
                seedPageData = res.data;
                renderSeedList(seedPageData);
                if (fruitPageData) {
                    renderOverview(seedPageData, fruitPageData);
                }
            } else {
                $("#storeSeedList").html("<div class='store-empty'>读取种子库存失败</div>");
                $("#storeSeedPager").empty();
            }
        }, function () {
            $("#storeSeedList").html("<div class='store-empty'>读取种子库存失败</div>");
            $("#storeSeedPager").empty();
        });

        FarmApi.fruitPage({
            userId: uid,
            page: state.fruitQuery.page,
            rows: state.fruitQuery.rows
        }, function (res) {
            if (FarmApi.isOk(res) && res.data) {
                fruitPageData = res.data;
                renderFruitList(fruitPageData);
                if (seedPageData) {
                    renderOverview(seedPageData, fruitPageData);
                }
            } else {
                $("#storeFruitList").html("<div class='store-empty'>读取果实库存失败</div>");
                $("#storeFruitPager").empty();
            }
        }, function () {
            $("#storeFruitList").html("<div class='store-empty'>读取果实库存失败</div>");
            $("#storeFruitPager").empty();
        });
    }

    function bindEvents() {
        $("#storeRefreshBtn").off("click.store").on("click.store", function () {
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
    if (window.FarmCore && $.isFunction(window.FarmCore.registerSetActiveModule)) {
        window.FarmCore.registerSetActiveModule("store", FarmStoreModule, {refreshMethod: "reload"});
    }
})(window, window.jQuery);
