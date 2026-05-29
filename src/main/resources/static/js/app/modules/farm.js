(function (window, $) {
    var FarmModule = {};
    var layout = {
        cols: 5,
        tileX: 118,
        tileY: 54,
        baseX: 460,
        baseY: 36
    };
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
            crop.remainWitherSeconds || 0
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
        var left = 36 + Math.round(offsetX * 0.68);
        var top = -86 + Math.round(offsetY * 0.68);
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
            setWsStatusUI("WS: ONLINE", "is-connected");
            stopPolling();
            return;
        }
        if (state.wsStatus === "connecting" || state.wsStatus === "reconnecting") {
            setWsStatusUI("WS: CONNECTING", "is-connecting");
            if (fallbackEnabled()) {
                startPolling();
            }
            return;
        }
        if (state.wsStatus === "closed" || state.wsStatus === "error") {
            setWsStatusUI("WS: OFFLINE", "is-disconnected");
            startPolling();
            return;
        }
        setWsStatusUI("WS: IDLE", "is-idle");
        if (state.wsStatus === "idle" || state.wsStatus === "stopped") {
            startPolling();
        }
    }

    function ensureActionDialog() {
        if ($("#farmActionDialog").length > 0) {
            return;
        }
        var html = "<div id='farmActionDialog' class='farm-action-dialog' style='display:none;'>" +
            "<div id='farmActionInfo'></div>" +
            "<div id='farmActionButtons' class='farm-action-buttons'></div>" +
            "</div>";
        $("body").append(html);
        $("#farmActionDialog").dialog({
            width: 420,
            height: 310,
            modal: true,
            closed: true,
            title: "地块操作"
        });
    }

    function ensureSeedDialog() {
        if ($("#farmSeedDialog").length > 0) {
            return;
        }
        var html = "<div id='farmSeedDialog' class='farm-action-dialog' style='display:none;'>" +
            "<div class='farm-action-row'>选择种子</div>" +
            "<div class='farm-action-row'><input id='farmSeedSelect' class='farm-seed-select'></div>" +
            "<div class='farm-action-buttons'>" +
            "<a id='farmSeedConfirmBtn' href='javascript:void(0)' class='easyui-linkbutton c1'>确认种植</a>" +
            "</div>" +
            "</div>";
        $("body").append(html);
        $("#farmSeedDialog").dialog({
            width: 420,
            height: 220,
            modal: true,
            closed: true,
            title: "种植"
        });
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

    function executeUnlock(plot) {
        FarmApi.plotUnlock({userId: currentUserId(), plotId: plot.plotId}, function (res) {
            if (!FarmApi.isOk(res)) {
                $.messager.alert("提示", (res && res.msg) ? res.msg : "解锁失败");
                playSound("error");
                return;
            }
            $.messager.show({title: "提示", msg: "地块解锁成功", timeout: motion().actionFeedbackMs, showType: "slide"});
            playSound("click");
            $("#farmActionDialog").dialog("close");
            postActionRefresh({plotId: plot.plotId, actionType: "unlock", tipText: "解锁成功"});
        }, function () {
            $.messager.alert("提示", "解锁失败，请稍后重试");
            playSound("error");
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
                $.messager.alert("提示", "暂无可用土壤类型");
                playSound("error");
                return;
            }
            var soilTypeId = asNumber(soils[0].id, 0);
            if (soilTypeId <= 0) {
                $.messager.alert("提示", "土壤配置异常");
                playSound("error");
                return;
            }
            FarmApi.plotExpand({userId: currentUserId(), soilTypeId: soilTypeId}, function (res) {
                if (!FarmApi.isOk(res)) {
                    $.messager.alert("提示", (res && res.msg) ? res.msg : "扩地失败");
                    playSound("error");
                    return;
                }
                $.messager.show({title: "提示", msg: "扩地成功", timeout: motion().actionFeedbackMs, showType: "slide"});
                playSound("click");
                $("#farmActionDialog").dialog("close");
                postActionRefresh({actionType: "expand", tipText: "扩地成功"});
            }, function () {
                $.messager.alert("提示", "扩地失败，请稍后重试");
                playSound("error");
            });
        });
    }

    function executeCare(plot) {
        FarmApi.care({userId: currentUserId(), plotId: plot.plotId}, function (res) {
            if (!FarmApi.isOk(res)) {
                $.messager.alert("提示", (res && res.msg) ? res.msg : "养护失败");
                playSound("error");
                return;
            }
            $.messager.show({title: "提示", msg: "养护完成", timeout: motion().actionFeedbackMs, showType: "slide"});
            playSound("care");
            $("#farmActionDialog").dialog("close");
            postActionRefresh({plotId: plot.plotId, actionType: "care", tipText: "养护完成"});
        }, function () {
            $.messager.alert("提示", "养护失败，请稍后重试");
            playSound("error");
        });
    }

    function executeHarvest(plot) {
        FarmApi.harvest({
            requestId: farmBuildRequestId("harvest"),
            userId: currentUserId(),
            plotId: plot.plotId
        }, function (res) {
            if (!FarmApi.isOk(res)) {
                $.messager.alert("提示", (res && res.msg) ? res.msg : "收获失败");
                playSound("error");
                return;
            }
            $.messager.show({title: "提示", msg: "收获成功", timeout: motion().actionFeedbackMs, showType: "slide"});
            playSound("harvest");
            $("#farmActionDialog").dialog("close");
            postActionRefresh({plotId: plot.plotId, actionType: "harvest", tipText: "收获成功"});
        }, function () {
            $.messager.alert("提示", "收获失败，请稍后重试");
            playSound("error");
        });
    }

    function openSeedDialog(plot) {
        ensureSeedDialog();
        FarmApi.myPlantingPanel(currentUserId(), function (res) {
            if (!(FarmApi.isOk(res) && res.data && $.isArray(res.data.seeds))) {
                $.messager.alert("提示", "读取种子背包失败");
                playSound("error");
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
                $.messager.alert("提示", "可种植的种子库存为空");
                playSound("error");
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

            $("#farmSeedConfirmBtn").off("click").on("click", function () {
                var seedTypeId = asNumber($("#farmSeedSelect").combobox("getValue"), 0);
                if (seedTypeId <= 0) {
                    $.messager.alert("提示", "请选择种子");
                    playSound("error");
                    return;
                }
                FarmApi.seedPlantablePlots(currentUserId(), seedTypeId, function (pRes) {
                    if (!(FarmApi.isOk(pRes) && pRes.data && $.isArray(pRes.data.plots))) {
                        $.messager.alert("提示", "验证可种地块失败");
                        playSound("error");
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
                        $.messager.alert("提示", "当前地块不可种此种子");
                        playSound("error");
                        return;
                    }
                    FarmApi.plant({
                        requestId: farmBuildRequestId("plant"),
                        userId: currentUserId(),
                        plotId: plot.plotId,
                        seedTypeId: seedTypeId
                    }, function (plantRes) {
                        if (!FarmApi.isOk(plantRes)) {
                            $.messager.alert("提示", (plantRes && plantRes.msg) ? plantRes.msg : "种植失败");
                            playSound("error");
                            return;
                        }
                        $.messager.show({title: "提示", msg: "种植成功", timeout: motion().actionFeedbackMs, showType: "slide"});
                        playSound("plant");
                        $("#farmSeedDialog").dialog("close");
                        $("#farmActionDialog").dialog("close");
                        postActionRefresh({plotId: plot.plotId, actionType: "plant", tipText: "种植成功"});
                    }, function () {
                        $.messager.alert("提示", "种植失败，请稍后重试");
                        playSound("error");
                    });
                }, function () {
                    $.messager.alert("提示", "验证可种地块失败");
                    playSound("error");
                });
            });
        }, function () {
            $.messager.alert("提示", "读取种子背包失败");
            playSound("error");
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
        $("#farmActionButtons [data-action='unlock']").off("click").on("click", function () {
            executeUnlock(plot);
        });
        $("#farmActionButtons [data-action='plant']").off("click").on("click", function () {
            openSeedDialog(plot);
        });
        $("#farmActionButtons [data-action='care']").off("click").on("click", function () {
            executeCare(plot);
        });
        $("#farmActionButtons [data-action='harvest']").off("click").on("click", function () {
            executeHarvest(plot);
        });
        $("#farmActionButtons [data-action='expand']").off("click").on("click", function () {
            executeExpand();
        });
        $("#farmActionButtons [data-action='refresh']").off("click").on("click", function () {
            loadOverviewByUser(currentUserId(), true);
        });

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
                $.messager.alert("提示", "该地块尚未解锁，请先在查看模式中解锁");
                playSound("error");
                return;
            }
            if (plot.hasCrop) {
                $.messager.alert("提示", "该地块已有作物，无法播种");
                playSound("error");
                return;
            }
            openSeedDialog(plot);
            return;
        }
        if (mode === "care") {
            if (plot.locked || !plot.hasCrop) {
                $.messager.alert("提示", "该地块暂无可杀虫作物");
                playSound("error");
                return;
            }
            if (!(plot.crop && plot.crop.canCare)) {
                $.messager.alert("提示", "当前作物没有虫害，不需要杀虫");
                playSound("error");
                return;
            }
            executeCare(plot);
            return;
        }
        if (mode === "clean") {
            if (plot.locked || !plot.hasCrop) {
                $.messager.alert("提示", "该地块暂无可铲除作物");
                playSound("error");
                return;
            }
            if (plot.crop && plot.crop.harvestable) {
                executeHarvest(plot);
                return;
            }
            $.messager.alert("提示", "当前后端未开放未成熟作物铲除接口，请先养护或等待收获");
            playSound("error");
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
        $(document).on("farm:realtime:overview", function (_, payload) {
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

        $(document).on("farm:realtime:status", function (_, text) {
            onRealtimeStatus(text);
        });
    }

    function bindEvents() {
        $("#farmFallbackSwitch").on("change", function () {
            fallbackEnabled();
            if ($(this).prop("checked")) {
                startPolling();
            } else {
                stopPolling();
            }
        });

        $("#farmToolBar").on("click", ".farm-tool-btn", function () {
            switchTool($(this).attr("data-tool"));
            playSound("click");
        });

        $("#farmIsoContainer").on("click", ".farm-plot", function () {
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
