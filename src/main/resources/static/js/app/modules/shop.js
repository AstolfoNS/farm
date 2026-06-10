(function (window, $) {
    var FarmShopModule = {};
    var state = {
        active: false,
        inited: false,
        userId: 0,
        loading: false,
        fruitLoaded: false,
        tradeLoaded: false,
        seedCardRows: [],
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
    var SEED_INFO_HOVER_DELAY_MS = 550;

    function motion() {
        if ($.isFunction(window.farmMotion)) {
            return window.farmMotion();
        }
        return {moduleEnterMs: 260, pageSwitchMs: 220, actionFeedbackMs: 1200};
    }

    function asNumber(value, def) {
        if (window.FarmUi && $.isFunction(window.FarmUi.asNumber)) {
            return window.FarmUi.asNumber(value, def);
        }
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function trimText(value) {
        if (window.FarmUi && $.isFunction(window.FarmUi.trimText)) {
            return window.FarmUi.trimText(value);
        }
        return $.trim(value == null ? "" : String(value));
    }

    function buildBuySuccessMessage(data) {
        var payload = data || {};
        var seedName = trimText(payload.seedName) || "种子";
        var buyQuantity = asNumber(payload.buyQuantity, 0);
        var totalCostCoin = asNumber(payload.totalCostCoin, 0);
        var afterCoin = asNumber(payload.afterCoin, 0);
        var afterSeedQuantity = asNumber(payload.afterSeedQuantity, 0);
        return "购买已完成：" + seedName + " x " + buyQuantity +
            "。\n已扣除金币 " + totalCostCoin +
            "，当前金币 " + afterCoin +
            "，该种子库存已更新为 " + afterSeedQuantity + "。";
    }

    function buildSellSuccessMessage(data) {
        var payload = data || {};
        var seedName = trimText(payload.seedName) || "果实";
        var sellQuantity = asNumber(payload.sellQuantity, 0);
        var totalIncomeCoin = asNumber(payload.totalIncomeCoin, 0);
        var afterCoin = asNumber(payload.afterCoin, 0);
        var afterFruitQuantity = asNumber(payload.afterFruitQuantity, 0);
        return "出售已完成：" + seedName + " x " + sellQuantity +
            "。\n本次获得金币 " + totalIncomeCoin +
            "，当前金币 " + afterCoin +
            "，剩余果实库存 " + afterFruitQuantity + "。";
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
        if (window.FarmUi && $.isFunction(window.FarmUi.escapeHtml)) {
            return window.FarmUi.escapeHtml(text);
        }
        return $("<div/>").text(text == null ? "" : String(text)).html();
    }

    function escapeAttr(text) {
        if (window.FarmUi && $.isFunction(window.FarmUi.escapeAttr)) {
            return window.FarmUi.escapeAttr(text);
        }
        var value = text == null ? "" : String(text);
        return value
            .replace(/&/g, "&amp;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;");
    }

    function toArray(list) {
        if (window.FarmUi && $.isFunction(window.FarmUi.toArray)) {
            return window.FarmUi.toArray(list);
        }
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
            loadFruitPage();
            return;
        }
        if (tabName === "trade") {
            $("#shopTradePane").addClass("is-active");
            loadTradePage();
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
            prompt: "按名称筛选"
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
        if (window.FarmUi && $.isFunction(window.FarmUi.totalPages)) {
            return window.FarmUi.totalPages(pageResult, 10);
        }
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
        return (window.farmDefaultAsset && window.farmDefaultAsset("seedCover")) || "";
    }

    function renderSeedList(pageData) {
        var records = toArray(pageData && pageData.records);
        state.seedCardRows = records;
        if (records.length === 0) {
            $("#shopSeedList").html("<div class='shop-empty'>暂无可购买种子</div>");
            $("#shopSeedPager").empty();
            return;
        }
        var html = [];
        $.each(records, function (_, row) {
            html.push(buildSeedCardHtml(row, _));
        });
        $("#shopSeedList").html(html.join(""));
        $("#shopSeedList .easyui-linkbutton").linkbutton();
        bindSeedActions();
        bindSeedHoverInfo();
        renderSeedPager(pageData);
    }

    function buildSeedCardHtml(row, index) {
        var seedTypeId = asNumber(row.id, 0);
        var price = asNumber(row.price, 0);
        var lockState = resolveSeedUnlockState(row);
        return "" +
            "<div class='shop-seed-card' data-seed-index='" + index + "'>" +
            "<div class='shop-seed-frame'>" +
            "<div class='shop-seed-head'>" +
            "<div class='shop-seed-name'>" + escapeHtml(row.name || ("种子#" + seedTypeId)) + "</div>" +
            "<div class='shop-seed-meta-row'>" +
            "<span>" + escapeHtml(row.seedQualityName || "-") + "</span>" +
            "<span>Lv." + asNumber(row.level, 0) + "</span>" +
            "</div>" +
            "</div>" +
            "<div class='shop-seed-cover-wrap'>" +
            "<img class='shop-seed-cover' src='" + escapeAttr(resolveCover(row.coverImageUrl)) + "' alt=''>" +
            "</div>" +
            "<div class='shop-seed-desc'>" + escapeHtml(row.description || "暂无描述") + "</div>" +
            "<div class='shop-seed-buy-info'>" +
            "<div class='shop-seed-price'>" +
            "<span>采购价</span><strong>" + price + "</strong><em>金币</em>" +
            "</div>" +
            "<div class='shop-seed-meta'>土地需求: " + escapeHtml(row.enableSoilTypeNames || "-") + "</div>" +
            "<div class='shop-seed-lock " + (lockState.unlocked ? "is-unlocked" : "is-locked") + "'>" + escapeHtml(lockState.text) + "</div>" +
            "</div>" +
            "</div>" +
            "<div class='shop-seed-actions'>" +
            "<a href='javascript:void(0)' class='easyui-linkbutton " + (lockState.unlocked ? "c1" : "c5") + " shop-buy-btn' data-seed-type-id='" + seedTypeId + "' data-price='" + price + "' data-seed-name='" + escapeAttr(row.name || "") + "' data-exp-locked='" + (lockState.unlocked ? "0" : "1") + "' " + (lockState.unlocked ? "" : "disabled='disabled'") + ">" + (lockState.unlocked ? "我要购买" : "经验未解锁") + "</a>" +
            "</div>" +
            "</div>";
    }

    function resolveSeedUnlockState(row) {
        var unlockRequired = asNumber(row && row.unlockExperienceRequired, 0);
        var currentExp = asNumber(row && row.currentUserExperience, 0);
        var unlocked = row && row.unlockedByExperience !== false && currentExp >= unlockRequired;
        var fallbackProgress = unlockRequired <= 0 ? 100 : Math.floor((currentExp * 100) / unlockRequired);
        var unlockProgress = Math.max(0, Math.min(100, asNumber(row && row.unlockProgressPercent, fallbackProgress)));
        return {
            unlocked: unlocked,
            text: unlocked
                ? "已解锁"
                : ("需经验 " + unlockRequired + "（当前 " + currentExp + "，进度 " + unlockProgress + "%）")
        };
    }

    function ensureSeedInfoTooltip() {
        var $tip = $("#shopSeedInfoTooltip");
        if ($tip.length > 0) {
            return $tip;
        }
        $("body").append("<div id='shopSeedInfoTooltip' class='shop-seed-info-tip' style='display:none;'></div>");
        return $("#shopSeedInfoTooltip");
    }

    function buildSeedInfoTooltipHtml(row) {
        var data = row || {};
        var quality = data.seedQualityName || "-";
        var level = asNumber(data.level, 0);
        var lockReq = asNumber(data.unlockExperienceRequired, 0);
        var growSeconds = asNumber(data.totalGrowSeconds, 0);
        var harvestFruit = asNumber(data.harvestFruitNumber, 0);
        var fruitPrice = asNumber(data.fruitPrice, 0);
        var harvestExp = asNumber(data.harvestExperience, 0);
        var harvestScore = asNumber(data.harvestScore, 0);
        var harvestTimes = asNumber(data.maxHarvestCount, 1);
        var bugLimit = asNumber(data.maxBugLimit, 0);
        var netValue = asNumber(data.estimatedNetValue, 0);
        var soilNames = data.enableSoilTypeNames || "-";
        var fruitLoss = asNumber(data.fruitLossPerBug, 0);
        var bugExp = asNumber(data.bugKillExperienceReward, 0);
        var bugScore = asNumber(data.bugKillScoreReward, 0);
        var bugCoin = asNumber(data.bugKillCoinReward, 0);
        var desc = data.description || "暂无描述";
        return "" +
            "<div class='shop-seed-info-title'>" + escapeHtml(data.name || "种子详情") + "</div>" +
            "<div class='shop-seed-info-subtitle'>" + escapeHtml(quality) + " | Lv." + level + "</div>" +
            "<div class='shop-seed-info-grid'>" + buildInfoGridRows([
                ["采购价", asNumber(data.price, 0) + " 金币"],
                ["解锁经验", lockReq],
                ["预估净值", netValue],
                ["总成长", growSeconds + " 秒"],
                ["单次果实", harvestFruit + " 个"],
                ["果实单价", fruitPrice + " 金币"],
                ["收获次数", harvestTimes + " 次"],
                ["虫害上限", bugLimit],
                ["收获经验", harvestExp],
                ["收获积分", harvestScore],
                ["虫害损失", fruitLoss],
                ["杀虫奖励", bugExp + " exp / " + bugScore + " 分 / " + bugCoin + " 金币"]
            ]) + "</div>" +
            "<div class='shop-seed-info-section'><span>土地需求</span><p>" + escapeHtml(soilNames) + "</p></div>" +
            "<div class='shop-seed-info-desc'>" + escapeHtml(desc) + "</div>";
    }

    function buildInfoGridRows(rows) {
        return $.map(rows || [], function (row) {
            return "<span>" + escapeHtml(row[0]) + "</span><b>" + escapeHtml(row[1]) + "</b>";
        }).join("");
    }

    function showSeedInfoTooltip($card, row) {
        var $tip = ensureSeedInfoTooltip();
        $tip.html(buildSeedInfoTooltipHtml(row)).show();
        var cardOffset = $card.offset();
        if (!cardOffset) {
            return;
        }
        var left = cardOffset.left + $card.outerWidth() + 8;
        var top = cardOffset.top + 6;
        var winLeft = $(window).scrollLeft();
        var winTop = $(window).scrollTop();
        var winWidth = $(window).width();
        var winHeight = $(window).height();
        var tipWidth = $tip.outerWidth();
        var tipHeight = $tip.outerHeight();
        if (left + tipWidth > winLeft + winWidth - 10) {
            left = cardOffset.left - tipWidth - 8;
        }
        if (left < winLeft + 8) {
            left = winLeft + 8;
        }
        if (top + tipHeight > winTop + winHeight - 10) {
            top = winTop + winHeight - tipHeight - 10;
        }
        if (top < winTop + 8) {
            top = winTop + 8;
        }
        $tip.css({left: left, top: top});
    }

    function hideSeedInfoTooltip() {
        $("#shopSeedInfoTooltip").hide();
    }

    function bindSeedHoverInfo() {
        var $cards = $("#shopSeedList .shop-seed-card");
        $cards.off(".seedHover");
        $cards.on("mouseenter.seedHover", function () {
            var $card = $(this);
            var hoverTimer = window.setTimeout(function () {
                var idx = asNumber($card.attr("data-seed-index"), -1);
                if (idx < 0 || idx >= state.seedCardRows.length) {
                    return;
                }
                showSeedInfoTooltip($card, state.seedCardRows[idx]);
            }, SEED_INFO_HOVER_DELAY_MS);
            $card.data("seedHoverTimer", hoverTimer);
        });
        $cards.on("mouseleave.seedHover", function () {
            var $card = $(this);
            var timer = $card.data("seedHoverTimer");
            if (timer) {
                window.clearTimeout(timer);
            }
            $card.removeData("seedHoverTimer");
            hideSeedInfoTooltip();
        });
    }

    function renderSeedPager(pageData) {
        if (window.FarmUi && $.isFunction(window.FarmUi.renderButtonPager)) {
            window.FarmUi.renderButtonPager({
                container: "#shopSeedPager",
                pageData: pageData,
                buttonClass: "shop-page-btn",
                prevClass: "shop-page-prev",
                nextClass: "shop-page-next",
                labelClass: "shop-page-label",
                onPrev: function () {
                    state.seedQuery.page = state.seedQuery.page - 1;
                    beginPageTransition();
                    reload();
                },
                onNext: function () {
                    state.seedQuery.page = state.seedQuery.page + 1;
                    beginPageTransition();
                    reload();
                }
            });
            return;
        }
        renderFallbackSeedPager(pageData);
    }

    function renderFallbackSeedPager(pageData) {
        var pageNo = asNumber(pageData && pageData.pageNo, 1);
        var pages = totalPages(pageData || {});
        var prevDisabled = pageNo <= 1 ? "disabled" : "";
        var nextDisabled = pageNo >= pages ? "disabled" : "";
        var html = "" +
            "<button type='button' class='shop-page-btn prev shop-page-prev' " + prevDisabled + "></button>" +
            "<span class='shop-page-label'>第 " + pageNo + " / " + pages + " 页</span>" +
            "<button type='button' class='shop-page-btn next shop-page-next' " + nextDisabled + "></button>";
        $("#shopSeedPager").html(html);
        $("#shopSeedPager .shop-page-prev").off("click.shopPager").on("click.shopPager", function () {
            if (state.seedQuery.page <= 1) {
                return;
            }
            state.seedQuery.page = state.seedQuery.page - 1;
            beginPageTransition();
            reload();
        });
        $("#shopSeedPager .shop-page-next").off("click.shopPager").on("click.shopPager", function () {
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
            $("#shopFruitList").html("<div class='shop-empty'>暂无种子库存</div>");
            return;
        }
        var html = [];
        $.each(records, function (_, row) {
            var seedTypeId = asNumber(row.seedTypeId, 0);
            var quantity = asNumber(row.quantity, 0);
            var frozen = asNumber(row.frozenQuantity, 0);
            var available = asNumber(row.availableQuantity, 0);
            var unitPrice = asNumber(row.unitBuyPrice, 0);
            var unlockRequired = asNumber(row.unlockExperienceRequired, 0);
            html.push(
                "<div class='shop-fruit-item'>" +
                "<div class='shop-fruit-name'>" + escapeHtml(row.seedName || ("种子#" + seedTypeId)) + "</div>" +
                "<div class='shop-fruit-meta'>库存: " + quantity + " | 冻结: " + frozen + " | 可用: " + available + "</div>" +
                "<div class='shop-fruit-meta'>采购价: " + unitPrice + " 金币 | 解锁经验: " + unlockRequired + "</div>" +
                "</div>"
            );
        });
        $("#shopFruitList").html(html.join(""));
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
            "<div id='shopBuyDialog' class='shop-action-dialog' style='display:none;'>" +
            "<div class='farm-dialog-shell shop-action-dialog-shell'>" +
            "<div class='shop-buy-summary'>" +
            "<div class='shop-buy-seed' id='shopBuySeedLabel'>种子</div>" +
            "<div class='shop-buy-cost' id='shopBuyCostTip'>0</div>" +
            "<div class='shop-buy-cost-label'>预计花费 金币</div>" +
            "</div>" +
            "<div class='shop-buy-fields'>" +
            "<div class='shop-buy-field'><span>购买数量</span><input id='shopBuyQty' style='width:150px;'></div>" +
            "<div class='shop-buy-field'><span>单价</span><b id='shopBuyUnitPrice'>0 金币</b></div>" +
            "<div class='shop-buy-field'><span>当前金币</span><b id='shopBuyCurrentCoin'>0</b></div>" +
            "</div>" +
            "</div>" +
            "</div>"
        );
        $("#shopBuyDialog").dialog({
            width: 420,
            height: 286,
            modal: true,
            closed: true,
            cls: "farm-dialog-window shop-dialog-window",
            onOpen: function () {
                var $panel = $(this).dialog("dialog");
                $panel.find(".dialog-button").addClass("farm-dialog-actions shop-dialog-actions");
            },
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
        var $buyButtons = $("#shopBuyDialog").dialog("dialog").find(".dialog-button .l-btn");
        $buyButtons.eq(0).addClass("c2");
        $buyButtons.eq(1).addClass("c5");
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
            "<div id='shopSellDialog' class='shop-action-dialog' style='display:none;'>" +
            "<div class='farm-dialog-shell shop-action-dialog-shell'>" +
            "<div class='farm-action-row' id='shopSellSeedLabel'>果实</div>" +
            "<div class='farm-action-row'>数量: <input id='shopSellQty' style='width:180px;'></div>" +
            "<div class='farm-action-row' id='shopSellIncomeTip'>预计获得: 0 金币</div>" +
            "</div>" +
            "</div>"
        );
        $("#shopSellDialog").dialog({
            width: 380,
            height: 230,
            modal: true,
            closed: true,
            cls: "farm-dialog-window shop-dialog-window",
            onOpen: function () {
                var $panel = $(this).dialog("dialog");
                $panel.find(".dialog-button").addClass("farm-dialog-actions shop-dialog-actions");
            },
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
        var $sellButtons = $("#shopSellDialog").dialog("dialog").find(".dialog-button .l-btn");
        $sellButtons.eq(0).addClass("c2");
        $sellButtons.eq(1).addClass("c5");
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
        $("#shopBuySeedLabel").text(seedName || ("种子#" + seedTypeId));
        $("#shopBuyUnitPrice").text(asNumber(price, 0) + " 金币");
        $("#shopBuyCurrentCoin").text($("#shopCurrentCoin").text() || "0");
        $("#shopBuyQty").numberbox("setValue", 1);
        $("#shopBuyCostTip").text(asNumber(price, 0));
        $("#shopBuyQty").numberbox({
            onChange: function (newVal) {
                var qty = asNumber(newVal, 1);
                if (qty <= 0) {
                    qty = 1;
                }
                $("#shopBuyCostTip").text(qty * asNumber(price, 0));
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
        $("#shopSellSeedLabel").text("果实: " + (seedName || ("种子#" + seedTypeId)) + " (最多 " + available + ")");
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
            $.messager.show({
                title: "提示",
                msg: buildBuySuccessMessage(res && res.data),
                timeout: motion().actionFeedbackMs,
                showType: "slide"
            });
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
            $.messager.show({
                title: "提示",
                msg: buildSellSuccessMessage(res && res.data),
                timeout: motion().actionFeedbackMs,
                showType: "slide"
            });
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
            if (String($btn.attr("data-exp-locked") || "0") === "1") {
                return;
            }
            try {
                if ($btn.linkbutton("options").disabled) {
                    return;
                }
            } catch (ignoreLinkOptionError) {}
            var seedTypeId = asNumber($btn.attr("data-seed-type-id"), 0);
            var seedName = $btn.attr("data-seed-name") || "";
            var price = asNumber($btn.attr("data-price"), 0);
            openBuyDialog(seedTypeId, seedName, price);
        });
    }

    function loadFruitPage() {
        if (!state.active || state.userId <= 0 || state.fruitLoaded) {
            return;
        }
        FarmApi.seedInventoryPage({
            userId: state.userId,
            page: state.fruitQuery.page,
            rows: state.fruitQuery.rows
        }, function (res) {
            if (!FarmApi.isOk(res)) {
                return;
            }
            state.fruitLoaded = true;
            renderFruitList(res.data);
        });
    }

    function loadTradePage() {
        if (!state.active || state.userId <= 0 || state.tradeLoaded) {
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
            state.tradeLoaded = true;
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
        state.fruitLoaded = false;
        state.tradeLoaded = false;
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
            if (state.currentTab === "fruit") {
                loadFruitPage();
            }
            if (state.currentTab === "trade") {
                loadTradePage();
            }
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
        hideSeedInfoTooltip();
        $("#shopPanel").removeClass("is-active");
    }

    FarmShopModule.setActive = setActive;
    FarmShopModule.reload = reload;

    window.FarmShopModule = FarmShopModule;
    if (window.FarmCore && $.isFunction(window.FarmCore.registerSetActiveModule)) {
        window.FarmCore.registerSetActiveModule("shop", FarmShopModule, {refreshMethod: "reload"});
    }
})(window, window.jQuery);


