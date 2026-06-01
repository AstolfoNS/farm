(function (window, $) {
    var FarmModule = {};
    var CROP_STATUS_WITHERED = 3;
    var WITHERED_CROP_IMAGE = "domain/farm/crops/crop-withered-default.png";
    var layout = {
        cols: 5,
        tileX: 118,
        tileY: 54,
        baseX: 460,
        baseY: 36
    };
    var cropAnchor = {
        left: 36,
        top: -86
    };
    var ActionKit = window.FarmActionKit || null;
    var state = {
        active: false,
        userId: 0,
        overview: null,
        plotSignatures: {},
        wsStatus: "idle",
        pollTimer: null,
        soilOptions: null,
        seedCoverById: {},
        seedVisualLoaded: false,
        selectedTool: "inspect"
    };
    var toolTitleMap = {
        inspect: "查看",
        plant: "播种",
        clean: "铲除",
        care: "杀虫"
    };

    function motion() {
        if ($.isFunction(window.farmMotion)) {
            return window.farmMotion();
        }
        return {moduleEnterMs: 260, pageSwitchMs: 220, actionFeedbackMs: 1200, dataRefreshDelayMs: 260};
    }

    function asNumber(value, def) {
        if (ActionKit && $.isFunction(ActionKit.asNumber)) {
            return ActionKit.asNumber(value, def);
        }
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function parsePixel(value, def) {
        var n = parseFloat(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function normalizeActionType(actionType) {
        var raw = String(actionType || "").toLowerCase();
        if (raw === "plant" || raw === "harvest" || raw === "care" || raw === "unlock" || raw === "expand") {
            return raw;
        }
        return "plant";
    }

    function asArray(list) {
        return $.isArray(list) ? list : [];
    }

    function playSound(key) {
        if (ActionKit && $.isFunction(ActionKit.playSound)) {
            ActionKit.playSound(key);
            return;
        }
        if (window.FarmAudio && $.isFunction(window.FarmAudio.play)) {
            window.FarmAudio.play(key);
        }
    }

    function currentUserId() {
        if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.currentUserId)) {
            return asNumber(window.FarmHomeBridge.currentUserId(), 0);
        }
        var user = window.FarmAppState.currentUser || {};
        return asNumber(user.id, 0);
    }

    function toolLabel(toolName) {
        var key = String(toolName || "").toLowerCase();
        return toolTitleMap[key] || "查看";
    }

    function switchTool(toolName) {
        var next = String(toolName || "inspect").toLowerCase();
        if (next !== "inspect" && next !== "plant" && next !== "clean" && next !== "care") {
            next = "inspect";
        }
        state.selectedTool = next;
        $(".farm-tool-btn").removeClass("is-active");
        $(".farm-tool-btn[data-tool='" + next + "']").addClass("is-active");
        if (next === "inspect") {
            $("#farmToolHint").text("当前工具：查看（点击地块打开操作窗）");
            return;
        }
        $("#farmToolHint").text("当前工具：" + toolLabel(next) + "（点击地块直接执行）");
    }

    function fallbackEnabled() {
        var checked = $("#farmFallbackSwitch").prop("checked") === true;
        window.FarmAppState.realtime.enableFallbackPolling = checked;
        return checked;
    }

    function pollIntervalMs() {
        return asNumber(window.FarmAppState.realtime.fallbackIntervalMs, 5000);
    }

    function cropStatusText(crop) {
        if (!crop) {
            return "空闲";
        }
        if (crop.harvestable) {
            return "可收获";
        }
        if (asNumber(crop.remainMatureSeconds, 0) > 0) {
            return "成熟倒计时 " + asNumber(crop.remainMatureSeconds, 0) + "s";
        }
        if (asNumber(crop.remainWitherSeconds, 0) > 0) {
            return "枯萎倒计时 " + asNumber(crop.remainWitherSeconds, 0) + "s";
        }
        return "生长中";
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

    function buildPlotSignature(plot) {
        var crop = plot && plot.crop ? plot.crop : {};
        return [
            plot.plotId,
            plot.locked ? 1 : 0,
            plot.hasCrop ? 1 : 0,
            crop.cropId || 0,
            crop.currentStageIndex || 0,
            crop.growStatus || 0,
            crop.bugCount || 0,
            crop.harvestable ? 1 : 0,
            crop.remainMatureSeconds || 0,
            crop.remainWitherSeconds || 0,
            crop.stageWidth || 0,
            crop.stageHeight || 0,
            crop.stageOffsetX || 0,
            crop.stageOffsetY || 0,
            crop.stageAssetUrl || ""
        ].join("|");
    }

    function calcIso(plotIndex) {
        var safeIndex = asNumber(plotIndex, 1);
        if (safeIndex <= 0) {
            safeIndex = 1;
        }
        var seq = safeIndex - 1;
        var col = seq % layout.cols;
        var row = Math.floor(seq / layout.cols);
        var x = layout.baseX + (col - row) * layout.tileX;
        var y = layout.baseY + (col + row) * layout.tileY;
        return {x: x, y: y};
    }

    function buildPlotClasses(plot) {
        var classes = ["farm-plot"];
        if (plot && plot.locked) {
            classes.push("is-locked");
        }
        if (plot && plot.hasCrop) {
            classes.push("is-has-crop");
        }
        if (plot && plot.crop && plot.crop.harvestable) {
            classes.push("is-harvestable");
        }
        var soilName = String((plot && plot.soilName) || "");
        if (soilName.indexOf("金") >= 0) {
            classes.push("is-soil-gold");
        } else if (soilName.indexOf("红") >= 0) {
            classes.push("is-soil-red");
        } else if (soilName.indexOf("黑") >= 0) {
            classes.push("is-soil-black");
        } else {
            classes.push("is-soil-base");
        }
        return classes.join(" ");
    }

    function buildPlotHtml(plot) {
        var crop = plot && plot.crop ? plot.crop : null;
        var pos = calcIso(plot.plotIndex);
        var safeId = plot.plotId || ("idx_" + asNumber(plot.plotIndex, 0));
        var title = plot.locked ? "未解锁地块" : (crop && crop.seedTypeName ? crop.seedTypeName : "空地");
        var subText = plot.locked ? (plot.lockReason || "待解锁") : cropStatusText(crop);
        var badges = [];
        var cropLayer = "";
        if (plot.locked) {
            badges.push("<span class='farm-plot-badge lock'>锁</span>");
        }
        if (crop && asNumber(crop.bugCount, 0) > 0) {
            badges.push("<span class='farm-plot-badge bug'><img src='" + escapeAttr(farmResolveImg("domain/farm/components/bug.png")) + "' alt='bug'><em>" + asNumber(crop.bugCount, 0) + "</em></span>");
        }
        if (crop && crop.harvestable) {
            badges.push("<span class='farm-plot-badge harvest'>熟</span>");
        }
        if (!plot.locked && plot.hasCrop && crop) {
            cropLayer = "<img class='farm-crop-sprite' style='" + escapeAttr(resolveCropStyle(crop)) + "' src='" + escapeAttr(resolveCropImage(crop)) + "' alt='crop'>";
        }

        return "<div id='farmPlot_" + safeId + "' class='" + buildPlotClasses(plot) + "' data-plot-id='" + safeId + "' style='left:" + pos.x + "px;top:" + pos.y + "px;'>" +
            "<div class='farm-plot-surface'></div>" +
            cropLayer +
            "<div class='farm-plot-content'>" +
            "<div class='farm-plot-title'>" + escapeHtml(title) + "</div>" +
            "<div class='farm-plot-sub'>" + escapeHtml(subText) + "</div>" +
            "</div>" +
            "<div class='farm-plot-badges'>" + badges.join("") + "</div>" +
            "</div>";
    }

    function resolveCropImage(crop) {
        if (asNumber(crop && crop.growStatus, 0) === CROP_STATUS_WITHERED) {
            return farmResolveImg(WITHERED_CROP_IMAGE);
        }
        if (crop && crop.stageAssetUrl && String(crop.stageAssetUrl).trim().length > 0) {
            var raw = String(crop.stageAssetUrl).trim();
            if (raw.indexOf("http://") === 0 || raw.indexOf("https://") === 0 || raw.indexOf("/") === 0) {
                return raw;
            }
            return "/" + raw;
        }
        var sid = asNumber(crop && crop.seedTypeId, 0);
        var cover = sid > 0 ? state.seedCoverById[String(sid)] : "";
        if (cover && String(cover).trim().length > 0) {
            return cover;
        }
        return farmResolveImg("domain/farm/actions/action-plant.png");
    }

    function resolveCropStyle(crop) {
        var width = asNumber(crop && crop.stageWidth, 132);
        var height = asNumber(crop && crop.stageHeight, 132);
        var offsetX = asNumber(crop && crop.stageOffsetX, 0);
        var offsetY = asNumber(crop && crop.stageOffsetY, 0);
        if (width <= 0) {
            width = 132;
        }
        if (height <= 0) {
            height = 132;
        }
        var left = cropAnchor.left + Math.round(offsetX);
        var top = cropAnchor.top + Math.round(offsetY);
        return "left:" + left + "px;top:" + top + "px;width:" + width + "px;height:" + height + "px;";
    }

    function ensureSeedVisuals() {
        if (state.seedVisualLoaded) {
            return;
        }
        state.seedVisualLoaded = true;
        FarmApi.shopPage({page: 1, rows: 100, sort: "id", order: "asc"}, function (res) {
            if (!(FarmApi.isOk(res) && res.data && $.isArray(res.data.records))) {
                return;
            }
            $.each(res.data.records, function (_, item) {
                var sid = asNumber(item.id, 0);
                var cover = item && item.coverImageUrl ? String(item.coverImageUrl) : "";
                if (sid > 0 && cover) {
                    state.seedCoverById[String(sid)] = cover;
                }
            });
            if (state.active && state.overview) {
                updateOverview(state.overview, true);
            }
        });
    }

    function renderMeta(overview) {
        var data = overview || {};
        $("#farmMetaBar [data-key='unlocked']").text(asNumber(data.unlockedPlots, 0));
        $("#farmMetaBar [data-key='total']").text(asNumber(data.totalPlots, 0));
        $("#farmMetaBar [data-key='occupied']").text(asNumber(data.occupiedPlots, 0));
        $("#farmMetaBar [data-key='harvestable']").text(asNumber(data.harvestableCount, 0));
    }

    function renderAll(overview) {
        var plots = asArray(overview && overview.plots);
        state.plotSignatures = {};
        if (plots.length === 0) {
            $("#farmIsoContainer").html("<div class='farm-empty-tip'>暂无地块数据，请先切换用户后重试。</div>");
            return;
        }

        var html = ["<div class='farm-iso-grid'>"];
        $.each(plots, function (_, plot) {
            var safeId = plot.plotId || ("idx_" + asNumber(plot.plotIndex, 0));
            state.plotSignatures[String(safeId)] = buildPlotSignature(plot);
            html.push(buildPlotHtml(plot));
        });
        html.push("</div>");
        $("#farmIsoContainer").html(html.join(""));
    }

    function patchChangedPlots(overview) {
        var plots = asArray(overview && overview.plots);
        var $grid = $("#farmIsoContainer .farm-iso-grid");
        if ($grid.length === 0 || plots.length === 0) {
            renderAll(overview);
            return;
        }

        var nextSignatures = {};
        $.each(plots, function (_, plot) {
            var safeId = plot.plotId || ("idx_" + asNumber(plot.plotIndex, 0));
            var idKey = String(safeId);
            var signature = buildPlotSignature(plot);
            var lastSignature = state.plotSignatures[idKey] || "";
            nextSignatures[idKey] = signature;
            if (signature === lastSignature) {
                return true;
            }
            var html = buildPlotHtml(plot);
            var $old = $("#farmPlot_" + safeId);
            if ($old.length > 0) {
                $old.replaceWith(html);
            } else {
                $grid.append(html);
            }
            return true;
        });

        if (Object.keys(state.plotSignatures).length !== Object.keys(nextSignatures).length) {
            renderAll(overview);
            return;
        }
        state.plotSignatures = nextSignatures;
    }

    function updateOverview(overview, forceFullRender) {
        if (!overview) {
            return;
        }
        state.overview = overview;
        state.userId = asNumber(overview.userId, 0);
        renderMeta(overview);
        if (forceFullRender) {
            renderAll(overview);
            return;
        }
        patchChangedPlots(overview);
    }

    function loadOverviewByUser(userId, forceFullRender) {
        var uid = asNumber(userId, 0);
        if (uid <= 0) {
            renderMeta({});
            renderAll({plots: []});
            return;
        }
        FarmApi.myFarmOverview(uid, function (res) {
            if (!(FarmApi.isOk(res) && res.data)) {
                return;
            }
            updateOverview(res.data, forceFullRender === true);
        });
    }

    function stopPolling() {
        if (state.pollTimer) {
            window.clearInterval(state.pollTimer);
            state.pollTimer = null;
        }
    }

    function startPolling() {
        stopPolling();
        if (!state.active || !fallbackEnabled()) {
            return;
        }
        var uid = currentUserId();
        if (uid <= 0) {
            return;
        }
        state.pollTimer = window.setInterval(function () {
            loadOverviewByUser(uid, false);
        }, pollIntervalMs());
    }

    function setWsStatusUI(statusText, cssName) {
        var $el = $("#farmWsStatus");
        $el.removeClass("is-connected is-connecting is-disconnected is-idle").addClass(cssName);
        $el.text(statusText);
    }

    function onRealtimeStatus(status) {
        state.wsStatus = status || "idle";
        if (state.wsStatus === "connected") {
            setWsStatusUI("实时同步: 已连接", "is-connected");
            stopPolling();
            return;
        }
        if (state.wsStatus === "connecting" || state.wsStatus === "reconnecting") {
            setWsStatusUI("实时同步: 连接中", "is-connecting");
            if (fallbackEnabled()) {
                startPolling();
            }
            return;
        }
        if (state.wsStatus === "closed" || state.wsStatus === "error") {
            setWsStatusUI("实时同步: 已切换自动刷新", "is-disconnected");
            startPolling();
            return;
        }
        setWsStatusUI("实时同步: 待连接", "is-idle");
        if (state.wsStatus === "idle" || state.wsStatus === "stopped") {
            startPolling();
        }
    }

    function ensureActionDialog() {
        var builder = function () {
            return "<div id='farmActionDialog' class='farm-action-dialog' style='display:none;'>" +
                "<div id='farmActionInfo'></div>" +
                "<div id='farmActionButtons' class='farm-action-buttons'></div>" +
                "</div>";
        };
        if (ActionKit && $.isFunction(ActionKit.ensureDialog)) {
            ActionKit.ensureDialog("#farmActionDialog", builder, {width: 420, height: 310, title: "地块操作"});
            return;
        }
        if ($("#farmActionDialog").length <= 0) {
            $("body").append(builder());
        }
        $("#farmActionDialog").dialog({width: 420, height: 310, modal: true, closed: true, title: "地块操作"});
    }

    function ensureSeedDialog() {
        var builder = function () {
            return "<div id='farmSeedDialog' class='farm-action-dialog' style='display:none;'>" +
                "<div class='farm-action-row'>选择种子</div>" +
                "<div class='farm-action-row'><input id='farmSeedSelect' class='farm-seed-select'></div>" +
                "<div class='farm-action-buttons'>" +
                "<a id='farmSeedConfirmBtn' href='javascript:void(0)' class='easyui-linkbutton c1'>确认种植</a>" +
                "</div>" +
                "</div>";
        };
        if (ActionKit && $.isFunction(ActionKit.ensureDialog)) {
            ActionKit.ensureDialog("#farmSeedDialog", builder, {width: 420, height: 220, title: "种植"});
            return;
        }
        if ($("#farmSeedDialog").length <= 0) {
            $("body").append(builder());
        }
        $("#farmSeedDialog").dialog({width: 420, height: 220, modal: true, closed: true, title: "种植"});
    }

    function findPlot(plotId) {
        var target = String(plotId || "");
        var result = null;
        $.each(asArray(state.overview && state.overview.plots), function (_, plot) {
            if (String(plot.plotId || "") === target) {
                result = plot;
                return false;
            }
            return true;
        });
        return result;
    }

    function animatePlotAction(plotId, actionType, tipText) {
        var safeId = String(plotId || "");
        if (!safeId) {
            return;
        }
        var safeActionType = normalizeActionType(actionType);
        var $plot = $("#farmPlot_" + safeId);
        if ($plot.length === 0) {
            return;
        }
        var animClass = "farm-plot-anim-success";
        if (safeActionType === "unlock" || safeActionType === "expand") {
            animClass = "farm-plot-anim-warning";
        }
        var feedbackMs = motion().actionFeedbackMs;
        $plot.removeClass("farm-plot-anim farm-plot-anim-success farm-plot-anim-warning")
            .addClass("farm-plot-anim " + animClass);
        window.setTimeout(function () {
            $plot.removeClass("farm-plot-anim farm-plot-anim-success farm-plot-anim-warning");
        }, feedbackMs);

        var $grid = $("#farmIsoContainer .farm-iso-grid");
        if ($grid.length === 0) {
            return;
        }
        var left = parsePixel($plot.css("left"), 0) + 82;
        var top = parsePixel($plot.css("top"), 0) - 24;
        var safeText = escapeHtml(tipText || "操作成功");
        var $tip = $("<div class='farm-plot-float-tip type-" + safeActionType + "'>" + safeText + "</div>");
        $tip.css({left: left + "px", top: top + "px"});
        $grid.append($tip);
        window.setTimeout(function () {
            $tip.addClass("is-show");
        }, 20);
        window.setTimeout(function () {
            $tip.remove();
        }, feedbackMs);
    }

    function animateGlobalAction(actionType, tipText) {
        var safeActionType = normalizeActionType(actionType);
        var $bar = $("#farmMetaBar");
        if ($bar.length === 0) {
            return;
        }
        var safeText = escapeHtml(tipText || "操作成功");
        var $tip = $("<div class='farm-plot-float-tip type-" + safeActionType + "'>" + safeText + "</div>");
        $tip.css({left: "12px", top: "42px"});
        $bar.css("position", "relative");
        $bar.append($tip);
        window.setTimeout(function () {
            $tip.addClass("is-show");
        }, 20);
        window.setTimeout(function () {
            $tip.remove();
        }, motion().actionFeedbackMs);
    }

    function postActionRefresh(options) {
        var opts = options || {};
        var delayMs = asNumber(opts.delayMs, motion().dataRefreshDelayMs);
        if (opts.plotId) {
            animatePlotAction(opts.plotId, opts.actionType, opts.tipText);
        } else if (opts.tipText) {
            animateGlobalAction(opts.actionType, opts.tipText);
        }
        var uid = currentUserId();
        window.setTimeout(function () {
            if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.refreshCurUser)) {
                window.FarmHomeBridge.refreshCurUser();
            }
            loadOverviewByUser(uid, true);
        }, delayMs);
    }

    function closeDialog(selector) {
        if (ActionKit && $.isFunction(ActionKit.closeDialog)) {
            ActionKit.closeDialog(selector);
            return;
        }
        $(selector).dialog("close");
    }

    function runFarmAction(options) {
        var opts = $.extend({
            request: null,
            successMessage: "",
            failMessage: "操作失败，请稍后重试",
            successSound: "click",
            afterSuccess: null
        }, options || {});

        if (ActionKit && $.isFunction(ActionKit.runAction)) {
            ActionKit.runAction({
                request: opts.request,
                successMessage: opts.successMessage,
                failMessage: opts.failMessage,
                successSound: opts.successSound,
                onSuccess: opts.afterSuccess
            });
            return;
        }

        opts.request(function (res) {
            if (!FarmApi.isOk(res)) {
                $.messager.alert("提示", (res && res.msg) ? res.msg : opts.failMessage);
                playSound("error");
                return;
            }
            $.messager.show({title: "提示", msg: opts.successMessage, timeout: motion().actionFeedbackMs, showType: "slide"});
            playSound(opts.successSound);
            if ($.isFunction(opts.afterSuccess)) {
                opts.afterSuccess(res);
            }
        }, function () {
            $.messager.alert("提示", opts.failMessage);
            playSound("error");
        });
    }

    function showActionError(message) {
        if (ActionKit && $.isFunction(ActionKit.alertError)) {
            ActionKit.alertError(message);
            playSound("error");
            return;
        }
        $.messager.alert("提示", message || "操作失败");
        playSound("error");
    }

    function executeUnlock(plot) {
        runFarmAction({
            request: function (ok, fail) {
                FarmApi.plotUnlock({userId: currentUserId(), plotId: plot.plotId}, ok, fail);
            },
            successMessage: "地块解锁成功",
            failMessage: "解锁失败，请稍后重试",
            successSound: "click",
            afterSuccess: function () {
                closeDialog("#farmActionDialog");
                postActionRefresh({plotId: plot.plotId, actionType: "unlock", tipText: "解锁成功"});
            }
        });
    }

    function loadSoilOptions(onDone) {
        if ($.isArray(state.soilOptions) && state.soilOptions.length > 0) {
            onDone(state.soilOptions);
            return;
        }
        FarmApi.listSoilOptions(function (res) {
            if (!(FarmApi.isOk(res) && $.isArray(res.data))) {
                onDone([]);
                return;
            }
            state.soilOptions = res.data;
            onDone(state.soilOptions);
        }, function () {
            onDone([]);
        });
    }

    function executeExpand() {
        loadSoilOptions(function (soils) {
            if (!soils || soils.length === 0) {
                showActionError("暂无可用土壤类型");
                return;
            }
            var soilTypeId = asNumber(soils[0].id, 0);
            if (soilTypeId <= 0) {
                showActionError("土壤配置异常");
                return;
            }
            runFarmAction({
                request: function (ok, fail) {
                    FarmApi.plotExpand({userId: currentUserId(), soilTypeId: soilTypeId}, ok, fail);
                },
                successMessage: "扩地成功",
                failMessage: "扩地失败，请稍后重试",
                successSound: "click",
                afterSuccess: function () {
                    closeDialog("#farmActionDialog");
                    postActionRefresh({actionType: "expand", tipText: "扩地成功"});
                }
            });
        });
    }

    function executeCare(plot) {
        runFarmAction({
            request: function (ok, fail) {
                FarmApi.care({userId: currentUserId(), plotId: plot.plotId}, ok, fail);
            },
            successMessage: "养护完成",
            failMessage: "养护失败，请稍后重试",
            successSound: "care",
            afterSuccess: function () {
                closeDialog("#farmActionDialog");
                postActionRefresh({plotId: plot.plotId, actionType: "care", tipText: "养护完成"});
            }
        });
    }

    function executeHarvest(plot) {
        runFarmAction({
            request: function (ok, fail) {
                FarmApi.harvest({
                    requestId: farmBuildRequestId("harvest"),
                    userId: currentUserId(),
                    plotId: plot.plotId
                }, ok, fail);
            },
            successMessage: "收获成功",
            failMessage: "收获失败，请稍后重试",
            successSound: "harvest",
            afterSuccess: function () {
                closeDialog("#farmActionDialog");
                postActionRefresh({plotId: plot.plotId, actionType: "harvest", tipText: "收获成功"});
            }
        });
    }

    function openSeedDialog(plot) {
        ensureSeedDialog();
        FarmApi.myPlantingPanel(currentUserId(), function (res) {
            if (!(FarmApi.isOk(res) && res.data && $.isArray(res.data.seeds))) {
                showActionError("读取种子背包失败");
                return;
            }
            var seeds = [];
            $.each(res.data.seeds, function (_, item) {
                var available = asNumber(item.availableQuantity, 0);
                if (item.selectable === true && available > 0) {
                    seeds.push({
                        seedTypeId: item.seedTypeId,
                        text: (item.seedTypeName || ("种子#" + item.seedTypeId)) + " (可用 " + available + ")"
                    });
                }
            });
            if (seeds.length === 0) {
                showActionError("可种植的种子库存为空");
                return;
            }
            $("#farmSeedSelect").combobox({
                valueField: "seedTypeId",
                textField: "text",
                editable: false,
                panelHeight: 180,
                data: seeds
            });
            $("#farmSeedSelect").combobox("setValue", seeds[0].seedTypeId);
            $("#farmSeedDialog").dialog("open");
            playSound("open");

            $("#farmSeedConfirmBtn").off("click.farmSeed").on("click.farmSeed", function () {
                var seedTypeId = asNumber($("#farmSeedSelect").combobox("getValue"), 0);
                if (seedTypeId <= 0) {
                    showActionError("请选择种子");
                    return;
                }
                FarmApi.seedPlantablePlots(currentUserId(), seedTypeId, function (pRes) {
                    if (!(FarmApi.isOk(pRes) && pRes.data && $.isArray(pRes.data.plots))) {
                        showActionError("校验可种地块失败");
                        return;
                    }
                    var allowed = false;
                    $.each(pRes.data.plots, function (_, item) {
                        if (String(item.plotId || "") === String(plot.plotId || "")) {
                            allowed = true;
                            return false;
                        }
                        return true;
                    });
                    if (!allowed) {
                        showActionError("当前地块不可种该种子");
                        return;
                    }

                    runFarmAction({
                        request: function (ok, fail) {
                            FarmApi.plant({
                                requestId: farmBuildRequestId("plant"),
                                userId: currentUserId(),
                                plotId: plot.plotId,
                                seedTypeId: seedTypeId
                            }, ok, fail);
                        },
                        successMessage: "种植成功",
                        failMessage: "种植失败，请稍后重试",
                        successSound: "plant",
                        afterSuccess: function () {
                            closeDialog("#farmSeedDialog");
                            closeDialog("#farmActionDialog");
                            postActionRefresh({plotId: plot.plotId, actionType: "plant", tipText: "种植成功"});
                        }
                    });
                }, function () {
                    showActionError("校验可种地块失败");
                });
            });
        }, function () {
            showActionError("读取种子背包失败");
        });
    }

    function buildActionButtons(plot) {
        var html = [];
        if (plot.locked && plot.canUnlock) {
            html.push("<a href='javascript:void(0)' class='easyui-linkbutton c1' data-action='unlock'>解锁地块</a>");
        }
        if (!plot.locked && !plot.hasCrop) {
            html.push("<a href='javascript:void(0)' class='easyui-linkbutton c1' data-action='plant'>种植</a>");
        }
        if (!plot.locked && plot.hasCrop && plot.crop && plot.crop.canCare) {
            html.push("<a href='javascript:void(0)' class='easyui-linkbutton c1' data-action='care'>养护除虫</a>");
        }
        if (!plot.locked && plot.hasCrop && plot.crop && plot.crop.harvestable) {
            html.push("<a href='javascript:void(0)' class='easyui-linkbutton c1' data-action='harvest'>收获</a>");
        }
        html.push("<a href='javascript:void(0)' class='easyui-linkbutton' data-action='expand'>扩地(默认土壤)</a>");
        html.push("<a href='javascript:void(0)' class='easyui-linkbutton' data-action='refresh'>刷新</a>");
        return html.join("");
    }

    function openPlotActionDialog(plotId) {
        var plot = findPlot(plotId);
        if (!plot) {
            return;
        }
        ensureActionDialog();
        var crop = plot.crop || {};
        var lines = [];
        lines.push("<div class='farm-action-row'><strong>地块#" + escapeHtml(plot.plotIndex) + "</strong></div>");
        lines.push("<div class='farm-action-row'>状态: " + escapeHtml(plot.locked ? "未解锁" : "已解锁") + "</div>");
        lines.push("<div class='farm-action-row'>土壤: " + escapeHtml(plot.soilName || "-") + "</div>");
        lines.push("<div class='farm-action-row'>作物: " + escapeHtml(plot.hasCrop ? (crop.seedTypeName || "已种植") : "空地") + "</div>");
        if (plot.hasCrop) {
            lines.push("<div class='farm-action-row'>详情: " + escapeHtml(cropStatusText(crop)) + "，虫子 " + escapeHtml(asNumber(crop.bugCount, 0)) + "</div>");
        }
        if (plot.locked && plot.lockReason) {
            lines.push("<div class='farm-action-row'>锁定原因: " + escapeHtml(plot.lockReason) + "</div>");
        }
        $("#farmActionInfo").html(lines.join(""));
        $("#farmActionButtons").html(buildActionButtons(plot));
        $("#farmActionButtons .easyui-linkbutton").linkbutton();

        if (ActionKit && $.isFunction(ActionKit.bindActionButtons)) {
            ActionKit.bindActionButtons("#farmActionButtons", {
                unlock: function () { executeUnlock(plot); },
                plant: function () { openSeedDialog(plot); },
                care: function () { executeCare(plot); },
                harvest: function () { executeHarvest(plot); },
                expand: function () { executeExpand(); },
                refresh: function () { loadOverviewByUser(currentUserId(), true); }
            }, ".farmDialogAction");
        } else {
            $("#farmActionButtons [data-action='unlock']").off("click.farmDialogAction").on("click.farmDialogAction", function () { executeUnlock(plot); });
            $("#farmActionButtons [data-action='plant']").off("click.farmDialogAction").on("click.farmDialogAction", function () { openSeedDialog(plot); });
            $("#farmActionButtons [data-action='care']").off("click.farmDialogAction").on("click.farmDialogAction", function () { executeCare(plot); });
            $("#farmActionButtons [data-action='harvest']").off("click.farmDialogAction").on("click.farmDialogAction", function () { executeHarvest(plot); });
            $("#farmActionButtons [data-action='expand']").off("click.farmDialogAction").on("click.farmDialogAction", function () { executeExpand(); });
            $("#farmActionButtons [data-action='refresh']").off("click.farmDialogAction").on("click.farmDialogAction", function () { loadOverviewByUser(currentUserId(), true); });
        }

        $("#farmActionDialog").dialog("open");
    }

    function applyToolOnPlot(plotId) {
        var plot = findPlot(plotId);
        if (!plot) {
            return;
        }
        var mode = state.selectedTool || "inspect";
        if (mode === "inspect") {
            openPlotActionDialog(plotId);
            return;
        }
        if (mode === "plant") {
            if (plot.locked) {
                showActionError("该地块尚未解锁，请先在查看模式中解锁");
                return;
            }
            if (plot.hasCrop) {
                showActionError("该地块已有作物，无法播种");
                return;
            }
            openSeedDialog(plot);
            return;
        }
        if (mode === "care") {
            if (plot.locked || !plot.hasCrop) {
                showActionError("该地块暂无可养护作物");
                return;
            }
            if (!(plot.crop && plot.crop.canCare)) {
                showActionError("当前作物没有虫害，不需要杀虫");
                return;
            }
            executeCare(plot);
            return;
        }
        if (mode === "clean") {
            if (plot.locked || !plot.hasCrop) {
                showActionError("该地块暂无可铲除作物");
                return;
            }
            if (plot.crop && plot.crop.harvestable) {
                executeHarvest(plot);
                return;
            }
            showActionError("当前后端未开放未成熟作物铲除接口，请先养护或等待收获");
            return;
        }
        openPlotActionDialog(plotId);
    }

    function setActive(flag) {
        state.active = !!flag;
        if (state.active) {
            $("#farmPanel").addClass("is-active is-module-enter");
            window.setTimeout(function () {
                $("#farmPanel").removeClass("is-module-enter");
            }, motion().moduleEnterMs);
            ensureSeedVisuals();
            switchTool(state.selectedTool || "inspect");
            loadOverviewByUser(currentUserId(), false);
            if (window.FarmWsBridge && window.FarmWsBridge.isConnected()) {
                onRealtimeStatus("connected");
            } else {
                onRealtimeStatus(state.wsStatus || "idle");
            }
            return;
        }
        $("#farmPanel").removeClass("is-active");
        stopPolling();
    }

    function bindRealtime() {
        $(document).off("farm:realtime:overview.farm").on("farm:realtime:overview.farm", function (_, payload) {
            if (!(payload && payload.overview)) {
                return;
            }
            var loginUid = currentUserId();
            var pushUid = asNumber(payload.userId, 0);
            if (loginUid <= 0 || (pushUid > 0 && pushUid !== loginUid)) {
                return;
            }
            updateOverview(payload.overview, false);
        });

        $(document).off("farm:realtime:status.farm").on("farm:realtime:status.farm", function (_, text) {
            onRealtimeStatus(text);
        });
    }

    function bindEvents() {
        $("#farmFallbackSwitch").off("change.farm").on("change.farm", function () {
            fallbackEnabled();
            if ($(this).prop("checked")) {
                startPolling();
            } else {
                stopPolling();
            }
        });

        $("#farmToolBar").off("click.farm", ".farm-tool-btn").on("click.farm", ".farm-tool-btn", function () {
            switchTool($(this).attr("data-tool"));
            playSound("click");
        });

        $("#farmIsoContainer").off("click.farm", ".farm-plot").on("click.farm", ".farm-plot", function () {
            var plotId = $(this).attr("data-plot-id");
            applyToolOnPlot(plotId);
        });
    }

    FarmModule.setActive = setActive;
    FarmModule.loadOverviewByUser = loadOverviewByUser;
    FarmModule.updateOverview = updateOverview;

    window.FarmModule = FarmModule;
    if (window.FarmCore && $.isFunction(window.FarmCore.registerSetActiveModule)) {
        window.FarmCore.registerSetActiveModule("farm", FarmModule, {
            refresh: function () {
                loadOverviewByUser(currentUserId(), false);
            }
        });
    }

    $(function () {
        bindRealtime();
        bindEvents();
        renderAll({plots: []});
        onRealtimeStatus("idle");
    });
})(window, window.jQuery);
