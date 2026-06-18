(function (window, $) {   var FarmModule = {};   var layout = {     cols: 5,     tileX: 118,     tileY: 54,     baseX: 460,     baseY: 36,   };   var cropAnchor = {     left: -8,     top: -170,   };   var STAGE_OFFSET_SCALE_X = 220 / 320;   var STAGE_OFFSET_SCALE_Y = 282 / 410;   var ActionKit = window.FarmActionKit || null;   var state = {     active: false,     userId: 0,     overview: null,     plotSignatures: {},     wsStatus: "idle",     pollTimer: null,     growthTimer: null,     transitionRefreshTimer: null,     lastTransitionRefreshAt: 0,     soilOptions: null,     seedCoverById: {},     seedVisualLoaded: false,     selectedTool: "inspect",   };   var toolTitleMap = {
    inspect: "查看",
    plant: "播种",
    harvest: "收获",
    clean: "铲除",
    care: "杀虫"
  };

function motion() {     if ($.isFunction(window.farmMotion)) {       return window.farmMotion();     }     return {       moduleEnterMs: 260,       pageSwitchMs: 220,       actionFeedbackMs: 1200,       dataRefreshDelayMs: 260,     };   }    function asNumber(value, def) {     if (ActionKit && $.isFunction(ActionKit.asNumber)) {       return ActionKit.asNumber(value, def);     }     var n = Number(value);     return isNaN(n) ? def || 0 : n;   }    function parsePixel(value, def) {     var n = parseFloat(value);     return isNaN(n) ? def || 0 : n;   }    function fmtNum(value) {     return asNumber(value, 0);   }   function buildUnlockSuccessMessage(res) {     var data = (res && res.data) || {};     return (       "地块解锁成功：第 " +       fmtNum(data.plotIndex) +       " 块地已开放。\n消耗金币 " +       fmtNum(data.unlockCostCoin) +       "，当前金币 " +       fmtNum(data.afterCoin) +       "，已解锁地块 " +       fmtNum(data.unlockedPlots) +       "/" +       fmtNum(data.totalPlots) +       "。"     );   }    function buildExpandSuccessMessage(res) {     var data = (res && res.data) || {};     return (       "扩地成功：新增第 " +       fmtNum(data.plotIndex) +       " 块地，土壤为 " +       String(data.soilName || "默认土壤") +       "。\n消耗金币 " +       fmtNum(data.expandCostCoin) +       "，当前金币 " +       fmtNum(data.afterCoin) +       "，当前地块总数 " +       fmtNum(data.totalPlots) +       "。"     );   }    function buildCareSuccessMessage(res) {     var data = (res && res.data) || {};     return (       "杀虫完成：清除虫害 " +       fmtNum(data.bugRemovedCount) +       " 只，剩余虫害 " +       fmtNum(data.bugCountAfter) +       "。\n获得经验 " +       fmtNum(data.experienceGain) +       "，积分 " +       fmtNum(data.scoreGain) +       "，金币 " +       fmtNum(data.coinGain) +       "；当前金币 " +       fmtNum(data.currentCoin) +       "。"     );   }    function buildHarvestSuccessMessage(res) {     var data = (res && res.data) || {};     return (       "收获完成：获得果实 " +       fmtNum(data.harvestFruitNumber) +       "，当前果实库存 " +       fmtNum(data.totalFruitQuantity) +       "。\n获得经验 " +       fmtNum(data.experienceGain) +       "，积分 " +       fmtNum(data.scoreGain) +       (fmtNum(data.totalBugPenaltyFruit) > 0         ? "；本次虫损 " + fmtNum(data.totalBugPenaltyFruit)         : "") +       "；当前经验 " +       fmtNum(data.currentExperience) +       "。"     );   }    function buildClearSuccessMessage(res) {     var data = (res && res.data) || {};     return (       "铲除完成：已清空当前作物。\n铲除前阶段 " +       fmtNum(data.stageIndexBefore) +       "，虫害数量 " +       fmtNum(data.bugCountBefore) +       "；当前地块已恢复为空地，可继续播种。"     );   }    function buildPlantSuccessMessage(res) {     var data = (res && res.data) || {};     return (       "播种成功：作物已进入第 " +       fmtNum(data.currentStageIndex) +       " 阶段。\n该种子剩余库存 " +       fmtNum(data.remainSeedQuantity) +       "，当前地块后续将按生长阶段自动推进。"     );   }    function plotLabel(plot) {     return "第 " + fmtNum(plot && plot.plotIndex) + " 块地";   }    function cropLabel(plot) {     var crop = plot && plot.crop ? plot.crop : {};     return String(crop.seedTypeName || "当前作物");   }    function buildFarmActionFailMessage(actionLabel, plot, reason) {     var detail = String(reason || "").trim() || "请检查当前状态与后端校验结果";     return actionLabel + "未完成：" + plotLabel(plot) + "，目标作物 " + cropLabel(plot) + "。\n失败原因：" + detail + "。";   }    function normalizeActionType(actionType) {     var raw = String(actionType || "").toLowerCase();     if (       raw === "plant" ||       raw === "harvest" ||       raw === "care" ||       raw === "clean" ||       raw === "unlock" ||       raw === "expand"     ) {       return raw;     }     return "plant";   }    function asArray(list) {     return $.isArray(list) ? list : [];   }    function playSound(key) {     if (ActionKit && $.isFunction(ActionKit.playSound)) {       ActionKit.playSound(key);       return;     }     if (window.FarmAudio && $.isFunction(window.FarmAudio.play)) {       window.FarmAudio.play(key);     }   }    function currentUserId() {     if (       window.FarmHomeBridge &&       $.isFunction(window.FarmHomeBridge.currentUserId)     ) {       return asNumber(window.FarmHomeBridge.currentUserId(), 0);     }     var user = window.FarmAppState.currentUser || {};     return asNumber(user.id, 0);   }    function toolLabel(toolName) {
    var key = String(toolName || "").toLowerCase();
    return toolTitleMap[key] || "查看";
  }

function defaultSoilCover() {     return (window.farmDefaultAsset && window.farmDefaultAsset("soilCover")) || "";   }    function applyToolCursor(toolName) {     var $iso = $("#farmIsoContainer");     if ($iso.length === 0) {       return;     }     $iso.removeClass(       "farm-cursor-inspect farm-cursor-plant farm-cursor-harvest farm-cursor-clean farm-cursor-care",     );     $iso.addClass("farm-cursor-" + String(toolName || "inspect"));   }    function switchTool(toolName) {
    var next = String(toolName || "inspect").toLowerCase();
    if (
      next !== "inspect" &&
      next !== "plant" &&
      next !== "harvest" &&
      next !== "clean" &&
      next !== "care"
    ) {
      next = "inspect";
    }
    state.selectedTool = next;
    $(".farm-tool-btn").removeClass("is-active");
    $(".farm-tool-btn[data-tool='" + next + "']").addClass("is-active");
    applyToolCursor(next);
    if (next === "inspect") {
      $("#farmToolHint").text("当前工具：查看（点击地块打开操作窗）");
      return;
    }
    $("#farmToolHint").text("当前工具：" + toolLabel(next) + "（点击地块直接执行）");
  }

function fallbackEnabled() {     var checked = $("#farmFallbackSwitch").prop("checked") === true;     window.FarmAppState.realtime.enableFallbackPolling = checked;     return checked;   }

  function pollIntervalMs() {
    return asNumber(window.FarmAppState.realtime.fallbackIntervalMs, 5000);
  }

  function growthTickIntervalMs() {
    var realtime = window.FarmAppState.realtime || {};
    return asNumber(realtime.growthTickIntervalMs, 1000);
  }

  function transitionRefreshDelayMs() {
    var realtime = window.FarmAppState.realtime || {};
    return asNumber(realtime.transitionRefreshDelayMs, 250);
  }

  function elapsedSnapshotSeconds(crop) {
    var snapshotAt = asNumber(crop && crop._farmSnapshotAt, 0);
    if (snapshotAt <= 0) {
      return 0;
    }
    return Math.max(0, Math.floor((new Date().getTime() - snapshotAt) / 1000));
  }

  function localRemainSeconds(crop, fieldName) {
    var base = asNumber(crop && crop[fieldName], 0);
    if (base <= 0) {
      return 0;
    }
    return Math.max(0, base - elapsedSnapshotSeconds(crop));
  }

  function cropStatusText(crop) {
    if (!crop) {
      return "空闲";
    }
    if (crop.harvestable) {
      return "可收获";
    }
    if (localRemainSeconds(crop, "remainMatureSeconds") > 0) {
      return "成熟倒计时 " + localRemainSeconds(crop, "remainMatureSeconds") + "s";
    }
    if (localRemainSeconds(crop, "remainWitherSeconds") > 0) {
      return "枯萎倒计时 " + localRemainSeconds(crop, "remainWitherSeconds") + "s";
    }
    return "生长中";
  }

function escapeHtml(text) {     return $("<div/>")       .text(text == null ? "" : String(text))       .html();   }    function escapeAttr(text) {     var value = text == null ? "" : String(text);     return value       .replace(/&/g, "&amp;")       .replace(/"/g, "&quot;")       .replace(/'/g, "&#39;")       .replace(/</g, "&lt;")       .replace(/>/g, "&gt;");   }    function buildPlotSignature(plot) {     var crop = plot && plot.crop ? plot.crop : {};     return [       plot.plotId,       plot.locked ? 1 : 0,       plot.hasCrop ? 1 : 0,       crop.cropId || 0,       crop.currentStageIndex || 0,       crop.growStatus || 0,       crop.bugCount || 0,       crop.harvestable ? 1 : 0,       crop.remainMatureSeconds || 0,       crop.remainWitherSeconds || 0,       crop.stageWidth || 0,       crop.stageHeight || 0,       crop.stageOffsetX || 0,       crop.stageOffsetY || 0,       crop.stageAssetUrl || "",       plot.soilCoverImageUrl || "",     ].join("|");   }    function calcIso(plotIndex) {     var safeIndex = asNumber(plotIndex, 1);     if (safeIndex <= 0) {       safeIndex = 1;     }     var seq = safeIndex - 1;     var col = seq % layout.cols;     var row = Math.floor(seq / layout.cols);     var x = layout.baseX + (col - row) * layout.tileX;     var y = layout.baseY + (col + row) * layout.tileY;     return { x: x, y: y };   }    function buildPlotClasses(plot) {     var classes = ["farm-plot"];     if (plot && plot.locked) {       classes.push("is-locked");     }     if (plot && plot.hasCrop) {       classes.push("is-has-crop");     }     if (plot && plot.crop && plot.crop.harvestable) {       classes.push("is-harvestable");     }     classes.push("is-soil-base");     return classes.join(" ");   }    function resolveSoilCoverImage(plot) {     var raw = $.trim(       plot && plot.soilCoverImageUrl ? String(plot.soilCoverImageUrl) : "",     );     if (!raw) {       return defaultSoilCover();     }     if (       raw.indexOf("http://") === 0 ||       raw.indexOf("https://") === 0 ||       raw.indexOf("/") === 0     ) {       return raw;     }     return "/" + raw;   }    function buildPlotHtml(plot) {
    var crop = plot && plot.crop ? plot.crop : null;
    var pos = calcIso(plot.plotIndex);
    var safeId = plot.plotId || "idx_" + asNumber(plot.plotIndex, 0);
    var title = plot.locked
      ? "未解锁地块"
      : crop && crop.seedTypeName
        ? crop.seedTypeName
        : "空地";
    var subText = plot.locked
      ? plot.lockReason || "待解锁"
      : cropStatusText(crop);
    var badges = [];
    var cropLayer = "";
    var bugOverlay = "";
    if (plot.locked) {
      badges.push("<span class='farm-plot-badge lock'>锁定</span>");
    }
    if (crop && asNumber(crop.bugCount, 0) > 0) {
      bugOverlay = buildBugOverlay(plot, crop);
    }
    if (crop && crop.harvestable) {
      badges.push("<span class='farm-plot-badge harvest'>熟</span>");
    }
    if (!plot.locked && plot.hasCrop && crop) {
      cropLayer =
        "<img class='farm-crop-sprite' style='" +
        escapeAttr(resolveCropStyle(crop)) +
        "' src='" +
        escapeAttr(resolveCropImage(crop)) +
        "' alt='crop'>" +
        bugOverlay;
    }

    var plotStyle =
      "left:" +
      pos.x +
      "px;top:" +
      pos.y +
      "px;background-image:url(" +
      escapeAttr(resolveSoilCoverImage(plot)) +
      ");";
    return (
      "<div id='farmPlot_" +
      safeId +
      "' class='" +
      buildPlotClasses(plot) +
      "' data-plot-id='" +
      safeId +
      "' style='" +
      plotStyle +
      "'>" +
      "<div class='farm-plot-surface'></div>" +
      cropLayer +
      "<div class='farm-plot-content'>" +
      "<div class='farm-plot-title'>" +
      escapeHtml(title) +
      "</div>" +
      "<div class='farm-plot-sub'>" +
      escapeHtml(subText) +
      "</div>" +
      "</div>" +
      "<div class='farm-plot-badges'>" +
      badges.join("") +
      "</div>" +
      "</div>"
    );
  }

function resolveCropImage(crop) {     if (       crop &&       crop.stageAssetUrl &&       String(crop.stageAssetUrl).trim().length > 0     ) {       var raw = String(crop.stageAssetUrl).trim();       if (         raw.indexOf("http://") === 0 ||         raw.indexOf("https://") === 0 ||         raw.indexOf("/") === 0       ) {         return raw;       }       return "/" + raw;     }     var sid = asNumber(crop && crop.seedTypeId, 0);     var cover = sid > 0 ? state.seedCoverById[String(sid)] : "";     if (cover && String(cover).trim().length > 0) {       return cover;     }     return farmResolveImg("domain/farm/actions/action-plant.png");   }    function resolveCropStyle(crop) {     var box = resolveCropBox(crop);     return (       "left:" +       box.left +       "px;top:" +       box.top +       "px;width:" +       box.width +       "px;height:" +       box.height +       "px;"     );   }    function resolveCropBox(crop) {     var width = asNumber(crop && crop.stageWidth, 132);     var height = asNumber(crop && crop.stageHeight, 132);     var offsetX = asNumber(crop && crop.stageOffsetX, 0);     var offsetY = asNumber(crop && crop.stageOffsetY, 0);     if (width <= 0) {       width = 132;     }     if (height <= 0) {       height = 132;     }     var renderWidth = Math.max(1, Math.round(width * STAGE_OFFSET_SCALE_X));     var renderHeight = Math.max(1, Math.round(height * STAGE_OFFSET_SCALE_Y));     var left = cropAnchor.left + Math.round(offsetX * STAGE_OFFSET_SCALE_X);     var top = cropAnchor.top + Math.round(offsetY * STAGE_OFFSET_SCALE_Y);     return {       left: left,       top: top,       width: renderWidth,       height: renderHeight,     };   }    function seededUnit(seed) {     var x = Math.sin(seed * 12.9898 + 78.233) * 43758.5453;     return x - Math.floor(x);   }    function buildBugOverlay(plot, crop) {     var count = asNumber(crop && crop.bugCount, 0);     if (count <= 0) {       return "";     }     var box = resolveCropBox(crop);     var maxDots = Math.min(4, Math.max(1, count));     var bugSrc = escapeAttr(farmResolveImg("domain/farm/components/bug.png"));     var html = ["<div class='farm-bug-layer'>"];     for (var i = 0; i < maxDots; i++) {       var seed =         asNumber(plot && plot.plotId, 0) * 17 +         asNumber(crop && crop.cropId, 0) * 13 +         i * 7;       var rx = seededUnit(seed);       var ry = seededUnit(seed + 3.33);       var bx = box.left + Math.round(box.width * (0.18 + rx * 0.64));       var by = box.top + Math.round(box.height * (0.1 + ry * 0.62));       html.push(         "<span class='farm-bug-dot' style='left:" +           bx +           "px;top:" +           by +           "px;'><img src='" +           bugSrc +           "' alt='bug'></span>",       );     }     html.push("<span class='farm-bug-count'>x" + count + "</span>");     html.push("</div>");     return html.join("");   }    function ensureSeedVisuals() {     if (state.seedVisualLoaded) {       return;     }     state.seedVisualLoaded = true;     FarmApi.shopPage(       { page: 1, rows: 100, sort: "id", order: "asc" },       function (res) {         if (!(FarmApi.isOk(res) && res.data && $.isArray(res.data.records))) {           return;         }         $.each(res.data.records, function (_, item) {           var sid = asNumber(item.id, 0);           var cover =             item && item.coverImageUrl ? String(item.coverImageUrl) : "";           if (sid > 0 && cover) {             state.seedCoverById[String(sid)] = cover;           }         });         if (state.active && state.overview) {           updateOverview(state.overview, true);         }       },     );   }    function renderMeta(overview) {     var data = overview || {};     $("#farmMetaBar [data-key='unlocked']").text(       asNumber(data.unlockedPlots, 0),     );     $("#farmMetaBar [data-key='total']").text(asNumber(data.totalPlots, 0));     $("#farmMetaBar [data-key='occupied']").text(       asNumber(data.occupiedPlots, 0),     );     $("#farmMetaBar [data-key='harvestable']").text(       asNumber(data.harvestableCount, 0),     );   }    function renderAll(overview) {
    var plots = asArray(overview && overview.plots);
    state.plotSignatures = {};
    if (plots.length === 0) {
      $("#farmIsoContainer").html(
        "<div class='farm-empty-tip'>暂无地块数据，请先切换用户后重试。</div>"
      );
      return;
    }

    var html = ["<div class='farm-iso-grid'>"];
    $.each(plots, function (_, plot) {
      var safeId = plot.plotId || "idx_" + asNumber(plot.plotIndex, 0);
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
      var safeId = plot.plotId || "idx_" + asNumber(plot.plotIndex, 0);
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

    if (
      Object.keys(state.plotSignatures).length !==
      Object.keys(nextSignatures).length
    ) {
      renderAll(overview);
      return;
    }
    state.plotSignatures = nextSignatures;
  }

  function stampRuntimeSnapshot(overview) {
    var snapshotAt = new Date().getTime();
    $.each(asArray(overview && overview.plots), function (_, plot) {
      if (!(plot && plot.crop)) {
        return true;
      }
      plot.crop._farmSnapshotAt = snapshotAt;
      plot.crop._farmTransitionRefreshRequested = false;
      return true;
    });
  }

  function updateOverview(overview, forceFullRender) {
    if (!overview) {
      return;
    }
    stampRuntimeSnapshot(overview);
    state.overview = overview;
    state.userId = asNumber(overview.userId, 0);
    renderMeta(overview);
    if (forceFullRender) {
      renderAll(overview);
    } else {
      patchChangedPlots(overview);
    }
    startGrowthTicker();
  }

  function loadOverviewByUser(userId, forceFullRender) {
    var uid = asNumber(userId, 0);
    if (uid <= 0) {
      renderMeta({});
      renderAll({ plots: [] });
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

  function stopGrowthTicker() {
    if (state.growthTimer) {
      window.clearInterval(state.growthTimer);
      state.growthTimer = null;
    }
    if (state.transitionRefreshTimer) {
      window.clearTimeout(state.transitionRefreshTimer);
      state.transitionRefreshTimer = null;
    }
  }

  function scheduleTransitionRefresh() {
    if (state.transitionRefreshTimer || !state.active) {
      return;
    }
    var now = new Date().getTime();
    if (now - state.lastTransitionRefreshAt < growthTickIntervalMs()) {
      return;
    }
    state.lastTransitionRefreshAt = now;
    state.transitionRefreshTimer = window.setTimeout(function () {
      state.transitionRefreshTimer = null;
      loadOverviewByUser(state.userId || currentUserId(), false);
    }, transitionRefreshDelayMs());
  }

  function refreshVisibleCropCountdowns() {
    var shouldRefresh = false;
    $.each(asArray(state.overview && state.overview.plots), function (_, plot) {
      if (!(plot && plot.crop)) {
        return true;
      }
      var crop = plot.crop;
      var safeId = plot.plotId || "idx_" + asNumber(plot.plotIndex, 0);
      $("#farmPlot_" + safeId + " .farm-plot-sub").text(cropStatusText(crop));

      var matureBase = asNumber(crop.remainMatureSeconds, 0);
      var witherBase = asNumber(crop.remainWitherSeconds, 0);
      var matureReached =
        matureBase > 0 && localRemainSeconds(crop, "remainMatureSeconds") <= 0;
      var witherReached =
        witherBase > 0 && localRemainSeconds(crop, "remainWitherSeconds") <= 0;

      if (
        (matureReached || witherReached) &&
        crop._farmTransitionRefreshRequested !== true
      ) {
        crop._farmTransitionRefreshRequested = true;
        shouldRefresh = true;
      }
      return true;
    });

    if (shouldRefresh) {
      scheduleTransitionRefresh();
    }
  }

  function startGrowthTicker() {
    if (!state.active || !(state.overview && asArray(state.overview.plots).length > 0)) {
      stopGrowthTicker();
      return;
    }
    refreshVisibleCropCountdowns();
    if (state.growthTimer) {
      return;
    }
    state.growthTimer = window.setInterval(function () {
      if (!state.active) {
        stopGrowthTicker();
        return;
      }
      refreshVisibleCropCountdowns();
    }, growthTickIntervalMs());
  }

  function setWsStatusUI(statusText, cssName) {
    var $el = $("#farmWsStatus");
    $el
      .removeClass("is-connected is-connecting is-disconnected is-idle")
      .addClass(cssName);
    $el.text(statusText);
  }

  function onRealtimeStatus(status) {
    state.wsStatus = status || "idle";
    if (state.wsStatus === "connected") {
      setWsStatusUI("农场状态：实时更新中", "is-connected");
      stopPolling();
      return;
    }
    if (state.wsStatus === "connecting" || state.wsStatus === "reconnecting") {
      setWsStatusUI("农场状态：连接中", "is-connecting");
      if (fallbackEnabled()) {
        startPolling();
      }
      return;
    }
    if (state.wsStatus === "closed" || state.wsStatus === "error") {
      setWsStatusUI("农场状态：自动刷新中", "is-disconnected");
      startPolling();
      return;
    }
    setWsStatusUI("农场状态：待命", "is-idle");
    if (state.wsStatus === "idle" || state.wsStatus === "stopped") {
      startPolling();
    }
  }

function ensureActionDialog() {
    var builder = function () {
      return (
        "<div id='farmActionDialog' class='farm-action-dialog' style='display:none;'>" +
        "<div id='farmActionInfo'></div>" +
        "<div id='farmActionButtons' class='farm-action-buttons'></div>" +
        "</div>"
      );
    };
    if (ActionKit && $.isFunction(ActionKit.ensureDialog)) {
      ActionKit.ensureDialog("#farmActionDialog", builder, {
        width: 420,
        height: 310,
        title: "地块操作",
        cls: "farm-dialog-window farm-dialog-shell"
      });
      return;
    }
    if ($("#farmActionDialog").length <= 0) {
      $("body").append(builder());
    }
    $("#farmActionDialog").dialog({
      width: 420,
      height: 310,
      modal: true,
      closed: true,
      title: "地块操作",
      cls: "farm-dialog-window farm-dialog-shell"
    });
  }

function ensureSeedDialog() {
    var builder = function () {
      return (
        "<div id='farmSeedDialog' class='farm-action-dialog' style='display:none;'>" +
        "<div id='farmSeedInfo'></div>" +
        "<div id='farmSeedButtons' class='farm-action-buttons'></div>" +
        "</div>"
      );
    };
    if (ActionKit && $.isFunction(ActionKit.ensureDialog)) {
      ActionKit.ensureDialog("#farmSeedDialog", builder, {
        width: 420,
        height: 220,
        title: "播种",
        cls: "farm-dialog-window farm-dialog-shell"
      });
      return;
    }
    if ($("#farmSeedDialog").length <= 0) {
      $("body").append(builder());
    }
    $("#farmSeedDialog").dialog({
      width: 420,
      height: 220,
      modal: true,
      closed: true,
      title: "播种",
      cls: "farm-dialog-window farm-dialog-shell"
    });
  }

function ensurePromptDialog() {
    var builder = function () {
      return (
        "<div id='farmPromptDialog' class='farm-action-dialog' style='display:none;'>" +
        "<div id='farmPromptInfo'></div>" +
        "<div id='farmPromptButtons' class='farm-action-buttons'></div>" +
        "</div>"
      );
    };
    if (ActionKit && $.isFunction(ActionKit.ensureDialog)) {
      ActionKit.ensureDialog("#farmPromptDialog", builder, {
        width: 400,
        height: 220,
        title: "提示",
        cls: "farm-dialog-window farm-dialog-shell"
      });
      return;
    }
    if ($("#farmPromptDialog").length <= 0) {
      $("body").append(builder());
    }
    $("#farmPromptDialog").dialog({
      width: 400,
      height: 220,
      modal: true,
      closed: true,
      title: "提示",
      cls: "farm-dialog-window farm-dialog-shell"
    });
  }

function ensureExpandDialog() {
    var builder = function () {
      return (
        "<div id='farmExpandDialog' class='farm-action-dialog farm-expand-dialog' style='display:none;'>" +
        "<div id='farmExpandSummary' class='farm-expand-summary'></div>" +
        "<div id='farmExpandList' class='farm-expand-list'></div>" +
        "<div id='farmExpandDetail' class='farm-expand-detail'></div>" +
        "<div id='farmExpandButtons' class='farm-action-buttons farm-expand-buttons'></div>" +
        "</div>"
      );
    };
    if (ActionKit && $.isFunction(ActionKit.ensureDialog)) {
      ActionKit.ensureDialog("#farmExpandDialog", builder, {
        width: 760,
        height: 540,
        title: "扩地",
        cls: "farm-dialog-window farm-dialog-shell"
      });
      return;
    }
    if ($("#farmExpandDialog").length <= 0) {
      $("body").append(builder());
    }
    $("#farmExpandDialog").dialog({
      width: 760,
      height: 540,
      modal: true,
      closed: true,
      title: "扩地",
      cls: "farm-dialog-window farm-dialog-shell"
    });
  }

function renderDialogTemplate(options) {
    var opts = $.extend(
      {
        dialogSelector: "",
        title: "",
        infoSelector: "",
        rows: [],
        actionSelector: "",
        buttons: []
      },
      options || {}
    );

    if (ActionKit && $.isFunction(ActionKit.renderDialogTemplate)) {
      ActionKit.renderDialogTemplate(opts);
      return;
    }

    if (opts.dialogSelector && opts.title) {
      $(opts.dialogSelector).dialog("setTitle", opts.title);
    }
    var html = [];
    $.each(opts.rows || [], function (_, row) {
      if (!row) {
        return true;
      }
      var content = row.html != null
        ? String(row.html)
        : row.label
          ? escapeHtml(row.label) + ": " + escapeHtml(row.value)
          : escapeHtml(row.value);
      if (row.strong === true) {
        content = "<strong>" + content + "</strong>";
      }
      var rowClass = "farm-action-row" + (row.className ? " " + row.className : "");
      html.push("<div class='" + rowClass + "'>" + content + "</div>");
      return true;
    });
    $(opts.infoSelector).html(html.join(""));

    var buttonHtml = [];
    $.each(opts.buttons || [], function (_, btn) {
      if (!btn || btn.visible === false || !btn.action || !btn.text) {
        return true;
      }
      var className = "easyui-linkbutton" + (btn.skin ? " " + btn.skin : "");
      var attrs = btn.id ? " id='" + btn.id + "'" : "";
      buttonHtml.push(
        "<a href='javascript:void(0)' class='" + className + "' data-action='" + btn.action + "'" + attrs + ">" + escapeHtml(btn.text) + "</a>"
      );
      return true;
    });
    $(opts.actionSelector).html(buttonHtml.join(""));
    $(opts.actionSelector + " .easyui-linkbutton").linkbutton();
  }

function findPlot(plotId) {     var target = String(plotId || "");     var result = null;     $.each(asArray(state.overview && state.overview.plots), function (_, plot) {       if (String(plot.plotId || "") === target) {         result = plot;         return false;       }       return true;     });     return result;   }    function animatePlotAction(plotId, actionType, tipText) {
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
    var $tip = $(
      "<div class='farm-plot-float-tip type-" +
      safeActionType +
      "'>" +
      safeText +
      "</div>"
    );
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
    var $tip = $(
      "<div class='farm-plot-float-tip type-" +
      safeActionType +
      "'>" +
      safeText +
      "</div>"
    );
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

function postActionRefresh(options) {     var opts = options || {};     var delayMs = asNumber(opts.delayMs, motion().dataRefreshDelayMs);     if (opts.plotId) {       animatePlotAction(opts.plotId, opts.actionType, opts.tipText);     } else if (opts.tipText) {       animateGlobalAction(opts.actionType, opts.tipText);     }     var uid = currentUserId();     window.setTimeout(function () {       if (         window.FarmHomeBridge &&         $.isFunction(window.FarmHomeBridge.refreshCurUser)       ) {         window.FarmHomeBridge.refreshCurUser();       }       loadOverviewByUser(uid, true);     }, delayMs);   }    function closeDialog(selector) {     if (ActionKit && $.isFunction(ActionKit.closeDialog)) {       ActionKit.closeDialog(selector);       return;     }     $(selector).dialog("close");   }    function openAndCenterDialog(selector) {     var $dialog = $(selector);     $dialog.dialog("open");     window.setTimeout(function () {       try {         $dialog.dialog("center");       } catch (ignoreCenterError) {}     }, 0);   }    function openPromptDialog(options) {
    var opts = $.extend({
      title: "提示",
      message: "",
      detail: "",
      strong: false,
      confirmText: "确定",
      cancelText: "取消",
      showCancel: false,
      onConfirm: null,
      onCancel: null
    }, options || {});
    ensurePromptDialog();
    var rows = [{
      value: opts.message || "",
      strong: opts.strong === true,
      className: "is-prompt"
    }];
    if (opts.detail) {
      rows.push({value: opts.detail, className: "is-prompt-detail"});
    }
    var buttons = [{action: "confirm", text: opts.confirmText || "确定", skin: "c1"}];
    if (opts.showCancel) {
      buttons.unshift({action: "cancel", text: opts.cancelText || "取消"});
    }
    renderDialogTemplate({
      dialogSelector: "#farmPromptDialog",
      title: opts.title || "提示",
      infoSelector: "#farmPromptInfo",
      rows: rows,
      actionSelector: "#farmPromptButtons",
      buttons: buttons
    });
    $("#farmPromptButtons").addClass("is-prompt-actions");
    var handlers = {
      confirm: function () {
        closeDialog("#farmPromptDialog");
        if ($.isFunction(opts.onConfirm)) {
          opts.onConfirm();
        }
      },
      cancel: function () {
        closeDialog("#farmPromptDialog");
        if ($.isFunction(opts.onCancel)) {
          opts.onCancel();
        }
      }
    };
    if (ActionKit && $.isFunction(ActionKit.bindActionButtons)) {
      ActionKit.bindActionButtons("#farmPromptButtons", handlers, ".farmPrompt");
    } else {
      $("#farmPromptButtons [data-action='confirm']").off("click.farmPrompt").on("click.farmPrompt", handlers.confirm);
      $("#farmPromptButtons [data-action='cancel']").off("click.farmPrompt").on("click.farmPrompt", handlers.cancel);
    }
    openAndCenterDialog("#farmPromptDialog");
    playSound("open");
  }

function confirmAction(options) {
    var opts = $.extend({
      title: "确认操作",
      message: "",
      detail: "",
      confirmText: "确认",
      cancelText: "取消",
      onConfirm: null
    }, options || {});
    openPromptDialog({
      title: opts.title,
      message: opts.message,
      detail: opts.detail,
      confirmText: opts.confirmText,
      cancelText: opts.cancelText,
      showCancel: true,
      onConfirm: opts.onConfirm
    });
  }

function runFarmAction(options) {
    var opts = $.extend({
      request: null,
      successMessage: "",
      successMessageBuilder: null,
      failMessage: "操作失败，请稍后重试",
      failMessageBuilder: null,
      successSound: "click",
      afterSuccess: null
    }, options || {});
    if (!$.isFunction(opts.request)) {
      return;
    }
    opts.request(function (res) {
      if (!FarmApi.isOk(res)) {
        var failText = res && res.msg ? res.msg : opts.failMessage;
        if ($.isFunction(opts.failMessageBuilder)) {
          failText = opts.failMessageBuilder(res) || failText;
        }
        showActionError(failText);
        return;
      }
      var finalSuccessMessage = opts.successMessage || "操作成功";
      if ($.isFunction(opts.successMessageBuilder)) {
        finalSuccessMessage = opts.successMessageBuilder(res) || finalSuccessMessage;
      }
      if (ActionKit && $.isFunction(ActionKit.toast)) {
        ActionKit.toast(finalSuccessMessage, motion().actionFeedbackMs);
      } else {
        $.messager.show({
          title: "提示",
          msg: finalSuccessMessage,
          timeout: motion().actionFeedbackMs,
          showType: "slide"
        });
      }
      playSound(opts.successSound);
      if ($.isFunction(opts.afterSuccess)) {
        opts.afterSuccess(res);
      }
    }, function () {
      var failText = opts.failMessage;
      if ($.isFunction(opts.failMessageBuilder)) {
        failText = opts.failMessageBuilder(null) || failText;
      }
      showActionError(failText);
    });
  }

function showActionError(message) {
    openPromptDialog({
      title: "操作提示",
      message: message || "操作失败",
      confirmText: "我知道了"
    });
    playSound("error");
  }

function executeUnlock(plot) {
    runFarmAction({
      request: function (ok, fail) {
        FarmApi.plotUnlock({userId: currentUserId(), plotId: plot.plotId}, ok, fail);
      },
      successMessage: "地块解锁成功",
      successMessageBuilder: buildUnlockSuccessMessage,
      failMessageBuilder: function (res) {
        return buildFarmActionFailMessage("瑙ｉ攣", plot, res && res.msg);
      },
      failMessage: "解锁失败，请稍后重试",
      successSound: "click",
      afterSuccess: function () {
        closeDialog("#farmActionDialog");
        postActionRefresh({plotId: plot.plotId, actionType: "unlock", tipText: "解锁成功"});
      }
    });
  }

function buildExpandOptionImage(option) {
    var raw = $.trim(option && option.coverImageUrl ? String(option.coverImageUrl) : "");
    if (!raw) {
      return defaultSoilCover();
    }
    if (raw.indexOf("http://") === 0 || raw.indexOf("https://") === 0 || raw.indexOf("/") === 0) {
      return raw;
    }
    return "/" + raw;
  }

function buildExpandStatus(option) {
    if (!option) {
      return {text: "不可扩地", className: "is-blocked"};
    }
    if (option.expandable) {
      return {text: "可扩建", className: "is-ready"};
    }
    if (option.unlockableByExperience === false && option.unlockableByCoin === false) {
      return {text: "经验与金币不足", className: "is-blocked"};
    }
    if (option.unlockableByExperience === false) {
      return {text: "经验不足", className: "is-blocked"};
    }
    if (option.unlockableByCoin === false) {
      return {text: "金币不足", className: "is-blocked"};
    }
    return {text: "暂不可扩建", className: "is-blocked"};
  }

function findExpandOptionById(data, soilTypeId) {
    var matched = null;
    $.each(asArray(data && data.options), function (_, option) {
      if (asNumber(option && option.soilTypeId, 0) === asNumber(soilTypeId, 0)) {
        matched = option;
        return false;
      }
      return true;
    });
    return matched;
  }

function resolveDefaultExpandOption(data, selectedSoilTypeId) {
    var selected = findExpandOptionById(data, selectedSoilTypeId);
    if (selected) {
      return selected;
    }
    var options = asArray(data && data.options);
    var firstExpandable = null;
    $.each(options, function (_, option) {
      if (option && option.expandable) {
        firstExpandable = option;
        return false;
      }
      return true;
    });
    return firstExpandable || (options.length > 0 ? options[0] : null);
  }

function renderExpandSummary(data) {
    return [
      "<div class='farm-expand-summary-grid'>",
      "<div class='farm-expand-summary-item'><span class='farm-expand-summary-label'>当前地块</span><strong>" + asNumber(data && data.currentTotalPlots, 0) + " 块</strong></div>",
      "<div class='farm-expand-summary-item'><span class='farm-expand-summary-label'>下一块编号</span><strong>#" + asNumber(data && data.nextPlotIndex, 0) + "</strong></div>",
      "<div class='farm-expand-summary-item'><span class='farm-expand-summary-label'>当前经验</span><strong>" + asNumber(data && data.currentExperience, 0) + "</strong></div>",
      "<div class='farm-expand-summary-item'><span class='farm-expand-summary-label'>当前金币</span><strong>" + asNumber(data && data.currentCoin, 0) + "</strong></div>",
      "</div>",
      "<div class='farm-expand-summary-tip'>请选择要扩展成哪种土壤。扩地消耗金币 <strong>" + asNumber(data && data.expandCostCoin, 0) + "</strong>，不同土壤还会额外校验经验门槛。</div>"
    ].join("");
  }

function renderExpandOptionCard(option, selectedSoilTypeId) {
    var optionId = asNumber(option && option.soilTypeId, 0);
    var status = buildExpandStatus(option);
    var cardClass = "farm-expand-option";
    if (optionId === asNumber(selectedSoilTypeId, 0)) {
      cardClass += " is-selected";
    }
    if (!(option && option.expandable)) {
      cardClass += " is-disabled";
    }
    return [
      "<div class='" + cardClass + "' data-soil-type-id='" + optionId + "'>",
      "<div class='farm-expand-option-cover'><img src='" + escapeAttr(buildExpandOptionImage(option)) + "' alt='soil'></div>",
      "<div class='farm-expand-option-body'>",
      "<div class='farm-expand-option-head'>",
      "<strong>" + escapeHtml(option && option.soilName ? option.soilName : "未命名土壤") + "</strong>",
      "<span class='farm-expand-option-status " + status.className + "'>" + escapeHtml(status.text) + "</span>",
      "</div>",
      "<div class='farm-expand-option-meta'>等级 " + asNumber(option && option.soilLevel, 0) + " · 经验要求 " + asNumber(option && option.unlockExperienceRequired, 0) + " · 金币 " + asNumber(option && option.expandCostCoin, 0) + "</div>",
      "<div class='farm-expand-option-desc'>" + escapeHtml(option && option.description ? option.description : "该土壤可用于新地块扩展。") + "</div>",
      "</div>",
      "</div>"
    ].join("");
  }

function renderExpandDetail(data, option) {
    if (!option) {
      return "<div class='farm-expand-detail-empty'>当前没有可用的扩地配置。</div>";
    }
    var status = buildExpandStatus(option);
    var expEnough = option.unlockableByExperience !== false;
    var coinEnough = option.unlockableByCoin !== false;
    return [
      "<div class='farm-expand-detail-title'>已选择：" + escapeHtml(option.soilName || "未命名土壤") + "</div>",
      "<div class='farm-expand-detail-row'><span>扩地结果</span><strong>新增第 #" + asNumber(data && data.nextPlotIndex, 0) + " 块地，土壤为 " + escapeHtml(option.soilName || "-") + "</strong></div>",
      "<div class='farm-expand-detail-row'><span>经验校验</span><strong class='" + (expEnough ? "is-pass" : "is-fail") + "'>当前 " + asNumber(data && data.currentExperience, 0) + " / 需要 " + asNumber(option.unlockExperienceRequired, 0) + "</strong></div>",
      "<div class='farm-expand-detail-row'><span>金币校验</span><strong class='" + (coinEnough ? "is-pass" : "is-fail") + "'>当前 " + asNumber(data && data.currentCoin, 0) + " / 需要 " + asNumber(option.expandCostCoin, 0) + "</strong></div>",
      "<div class='farm-expand-detail-row'><span>当前状态</span><strong class='" + status.className + "'>" + escapeHtml(status.text) + "</strong></div>"
    ].join("");
  }

function renderExpandDialog(data, selectedSoilTypeId) {
    var selected = resolveDefaultExpandOption(data, selectedSoilTypeId);
    var selectedId = asNumber(selected && selected.soilTypeId, 0);
    $("#farmExpandDialog").data("expandPayload", data || {});
    $("#farmExpandDialog").data("selectedSoilTypeId", selectedId);
    $("#farmExpandSummary").html(renderExpandSummary(data));
    $("#farmExpandList").html($.map(asArray(data && data.options), function (option) {
      return renderExpandOptionCard(option, selectedId);
    }).join(""));
    $("#farmExpandDetail").html(renderExpandDetail(data, selected));
    $("#farmExpandButtons").html(
      "<a href='javascript:void(0)' class='easyui-linkbutton' data-action='refresh-expand'>刷新选项</a>" +
      "<a id='farmExpandConfirmBtn' href='javascript:void(0)' class='easyui-linkbutton c1' data-action='confirm-expand'>确认扩地</a>"
    );
    $("#farmExpandButtons .easyui-linkbutton").linkbutton();
    if (selected && selected.expandable) {
      $("#farmExpandConfirmBtn").linkbutton("enable");
    } else {
      $("#farmExpandConfirmBtn").linkbutton("disable");
    }
  }

function bindExpandDialog(data) {
    $("#farmExpandList")
      .off("click.farmExpand", ".farm-expand-option")
      .on("click.farmExpand", ".farm-expand-option", function () {
        var soilTypeId = asNumber($(this).attr("data-soil-type-id"), 0);
        renderExpandDialog(data, soilTypeId);
      });

    var handlers = {
      "refresh-expand": function () {
        openExpandDialog($("#farmExpandDialog").data("selectedSoilTypeId"));
      },
      "confirm-expand": function () {
        var dialogData = $("#farmExpandDialog").data("expandPayload") || data || {};
        var selectedOption = findExpandOptionById(dialogData, $("#farmExpandDialog").data("selectedSoilTypeId"));
        if (!selectedOption) {
          showActionError("请先选择要扩展的土壤类型。");
          return;
        }
        if (!selectedOption.expandable) {
          showActionError("当前条件不足，无法扩展为所选土壤。请先补足经验或金币。");
          return;
        }
        runFarmAction({
          request: function (ok, fail) {
            FarmApi.plotExpand({userId: currentUserId(), soilTypeId: selectedOption.soilTypeId}, ok, fail);
          },
          successMessage: "扩地成功",
          successMessageBuilder: buildExpandSuccessMessage,
          failMessageBuilder: function (res) {
            var reason = res && res.msg ? res.msg : "请检查当前经验、金币和土壤配置";
            return "扩地未完成：目标土壤为「" + String(selectedOption.soilName || "未命名土壤") + "」。\n失败原因：" + reason + "。";
          },
          failMessage: "扩地失败，请稍后重试",
          successSound: "click",
          afterSuccess: function () {
            closeDialog("#farmExpandDialog");
            closeDialog("#farmActionDialog");
            postActionRefresh({actionType: "expand", tipText: "扩地成功"});
          }
        });
      }
    };

    if (ActionKit && $.isFunction(ActionKit.bindActionButtons)) {
      ActionKit.bindActionButtons("#farmExpandButtons", handlers, ".farmExpand");
    } else {
      $("#farmExpandButtons [data-action='refresh-expand']").off("click.farmExpand").on("click.farmExpand", handlers["refresh-expand"]);
      $("#farmExpandButtons [data-action='confirm-expand']").off("click.farmExpand").on("click.farmExpand", handlers["confirm-expand"]);
    }
  }

function openExpandDialog(selectedSoilTypeId) {
    ensureExpandDialog();
    FarmApi.plotExpandOptions({userId: currentUserId()}, function (res) {
      if (!(FarmApi.isOk(res) && res.data)) {
        showActionError(res && res.msg ? res.msg : "读取扩地选项失败");
        return;
      }
      var data = res.data || {};
      if (!$.isArray(data.options) || data.options.length === 0) {
        showActionError("当前还没有可用的土壤类型，暂时无法扩地。");
        return;
      }
      renderExpandDialog(data, selectedSoilTypeId);
      bindExpandDialog(data);
      openAndCenterDialog("#farmExpandDialog");
      playSound("open");
    }, function () {
      showActionError("读取扩地选项失败，请稍后重试。");
    });
  }

function executeExpand() {
    openExpandDialog();
  }

function executeCare(plot) {
    runFarmAction({
      request: function (ok, fail) {
        FarmApi.care({userId: currentUserId(), plotId: plot.plotId}, ok, fail);
      },
      successMessage: "养护完成",
      successMessageBuilder: buildCareSuccessMessage,
      failMessageBuilder: function (res) {
        return buildFarmActionFailMessage("养护", plot, res && res.msg);
      },
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
      successMessageBuilder: buildHarvestSuccessMessage,
      failMessageBuilder: function (res) {
        return buildFarmActionFailMessage("收获", plot, res && res.msg);
      },
      failMessage: "收获失败，请稍后重试",
      successSound: "harvest",
      afterSuccess: function () {
        closeDialog("#farmActionDialog");
        postActionRefresh({plotId: plot.plotId, actionType: "harvest", tipText: "收获成功"});
      }
    });
  }

function executeClear(plot) {
    runFarmAction({
      request: function (ok, fail) {
        FarmApi.clear({
          requestId: farmBuildRequestId("clear"),
          userId: currentUserId(),
          plotId: plot.plotId
        }, ok, fail);
      },
      successMessage: "铲除成功",
      successMessageBuilder: buildClearSuccessMessage,
      failMessageBuilder: function (res) {
        return buildFarmActionFailMessage("铲除", plot, res && res.msg);
      },
      failMessage: "铲除失败，请稍后重试",
      successSound: "clean",
      afterSuccess: function () {
        closeDialog("#farmActionDialog");
        postActionRefresh({plotId: plot.plotId, actionType: "clean", tipText: "铲除成功"});
      }
    });
  }

function openSeedDialog(plot) {
    ensureSeedDialog();
    renderDialogTemplate({
      dialogSelector: "#farmSeedDialog",
      title: "播种",
      infoSelector: "#farmSeedInfo",
      rows: [
        {value: "选择种子", strong: true},
        {html: "<input id='farmSeedSelect' class='farm-seed-select'>"}
      ],
      actionSelector: "#farmSeedButtons",
      buttons: [
        {action: "confirm-seed", text: "确认播种", skin: "c1", id: "farmSeedConfirmBtn"}
      ]
    });

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
            text: (item.seedTypeName || "种子#" + item.seedTypeId) + " (可用 " + available + ")"
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
      openAndCenterDialog("#farmSeedDialog");
      playSound("open");

      var onConfirmPlant = function () {
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
            successMessage: "播种成功",
            successMessageBuilder: buildPlantSuccessMessage,
            failMessageBuilder: function (response) {
              return buildFarmActionFailMessage("播种", plot, response && response.msg);
            },
            failMessage: "播种失败，请稍后重试",
            successSound: "plant",
            afterSuccess: function () {
              closeDialog("#farmSeedDialog");
              closeDialog("#farmActionDialog");
              postActionRefresh({plotId: plot.plotId, actionType: "plant", tipText: "播种成功"});
            }
          });
        }, function () {
          showActionError("校验可种地块失败");
        });
      };

      if (ActionKit && $.isFunction(ActionKit.bindActionButtons)) {
        ActionKit.bindActionButtons("#farmSeedButtons", {"confirm-seed": onConfirmPlant}, ".farmSeed");
      } else {
        $("#farmSeedButtons [data-action='confirm-seed']").off("click.farmSeed").on("click.farmSeed", function () {
          onConfirmPlant();
        });
      }
    }, function () {
      showActionError("读取种子背包失败");
    });
  }

function buildPlotActionButtons(plot) {
    var buttons = [];
    if (plot.locked && plot.canUnlock) {
      buttons.push({action: "unlock", text: "解锁地块", skin: "c1"});
    }
    if (!plot.locked && !plot.hasCrop) {
      buttons.push({action: "plant", text: "播种", skin: "c1"});
    }
    if (!plot.locked && plot.hasCrop && plot.crop && plot.crop.canCare) {
      buttons.push({action: "care", text: "养护杀虫", skin: "c1"});
    }
    if (!plot.locked && plot.hasCrop && plot.crop && plot.crop.harvestable) {
      buttons.push({action: "harvest", text: "收获", skin: "c1"});
    }
    if (!plot.locked && plot.hasCrop && plot.crop && !plot.crop.harvestable) {
      buttons.push({action: "clear", text: "铲除作物", skin: "c5"});
    }
    buttons.push({action: "expand", text: "扩地", skin: "c2"});
    buttons.push({action: "refresh", text: "刷新"});
    return buttons;
  }

function buildPlotActionRows(plot) {
    var crop = plot.crop || {};
    var rows = [
      {value: "地块#" + (plot.plotIndex == null ? "-" : plot.plotIndex), strong: true},
      {label: "状态", value: plot.locked ? "未解锁" : "已解锁"},
      {label: "土壤", value: plot.soilName || "-"},
      {label: "作物", value: plot.hasCrop ? crop.seedTypeName || "已种植" : "空地"}
    ];
    if (plot.hasCrop) {
      rows.push({label: "详情", value: cropStatusText(crop) + "，虫害 " + asNumber(crop.bugCount, 0)});
    }
    if (plot.locked && plot.lockReason) {
      rows.push({label: "锁定原因", value: plot.lockReason});
    }
    if (plot.locked) {
      rows.push({label: "解锁条件", value: "经验 " + asNumber(plot.unlockRequiredExperience, 0) + "，金币 " + asNumber(plot.unlockCostCoin, 0)});
      if (plot.unlockableByExperience === false) {
        rows.push({label: "当前不可解锁", value: "经验不足"});
      } else if (plot.unlockableByCoin === false) {
        rows.push({label: "当前不可解锁", value: "金币不足"});
      } else if (plot.canUnlock === false) {
        rows.push({label: "当前不可解锁", value: "需要先解锁前置地块"});
      }
    }
    return rows;
  }

function openPlotActionDialog(plotId) {
    var plot = findPlot(plotId);
    if (!plot) {
      return;
    }
    ensureActionDialog();
    renderDialogTemplate({
      dialogSelector: "#farmActionDialog",
      title: "地块操作",
      infoSelector: "#farmActionInfo",
      rows: buildPlotActionRows(plot),
      actionSelector: "#farmActionButtons",
      buttons: buildPlotActionButtons(plot)
    });

    if (ActionKit && $.isFunction(ActionKit.bindActionButtons)) {
      ActionKit.bindActionButtons("#farmActionButtons", {
        unlock: function () { executeUnlock(plot); },
        plant: function () { openSeedDialog(plot); },
        care: function () { executeCare(plot); },
        harvest: function () { executeHarvest(plot); },
        clear: function () { executeClear(plot); },
        expand: function () { executeExpand(); },
        refresh: function () { loadOverviewByUser(currentUserId(), true); }
      }, ".farmDialogAction");
    } else {
      $("#farmActionButtons [data-action='unlock']").off("click.farmDialogAction").on("click.farmDialogAction", function () { executeUnlock(plot); });
      $("#farmActionButtons [data-action='plant']").off("click.farmDialogAction").on("click.farmDialogAction", function () { openSeedDialog(plot); });
      $("#farmActionButtons [data-action='care']").off("click.farmDialogAction").on("click.farmDialogAction", function () { executeCare(plot); });
      $("#farmActionButtons [data-action='harvest']").off("click.farmDialogAction").on("click.farmDialogAction", function () { executeHarvest(plot); });
      $("#farmActionButtons [data-action='clear']").off("click.farmDialogAction").on("click.farmDialogAction", function () { executeClear(plot); });
      $("#farmActionButtons [data-action='expand']").off("click.farmDialogAction").on("click.farmDialogAction", function () { executeExpand(); });
      $("#farmActionButtons [data-action='refresh']").off("click.farmDialogAction").on("click.farmDialogAction", function () { loadOverviewByUser(currentUserId(), true); });
    }

    openAndCenterDialog("#farmActionDialog");
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
        showActionError("该地块尚未解锁，请先在查看模式中完成解锁。");
        return;
      }
      if (plot.hasCrop) {
        showActionError("该地块已有作物，当前不能继续播种。");
        return;
      }
      openSeedDialog(plot);
      return;
    }
    if (mode === "care") {
      if (plot.locked || !plot.hasCrop) {
        showActionError("该地块当前没有可养护的作物。");
        return;
      }
      if (!(plot.crop && plot.crop.canCare)) {
        showActionError("当前作物没有虫害，不需要执行杀虫。");
        return;
      }
      executeCare(plot);
      return;
    }
    if (mode === "harvest") {
      if (plot.locked || !plot.hasCrop) {
        showActionError("该地块当前没有可收获的作物。");
        return;
      }
      if (!(plot.crop && plot.crop.harvestable)) {
        showActionError("当前作物尚未成熟，暂时不能收获。");
        return;
      }
      executeHarvest(plot);
      return;
    }
    if (mode === "clean") {
      if (plot.locked || !plot.hasCrop) {
        showActionError("该地块当前没有可铲除的作物。");
        return;
      }
      confirmAction({
        title: "确认铲除",
        message: plot.crop && plot.crop.harvestable ? "该作物已经成熟，确认直接铲除吗？" : "该作物尚未成熟，确认直接铲除吗？",
        detail: "铲除后不会获得果实、经验与积分。" + (plot.crop && plot.crop.harvestable ? "建议先使用“收获”工具完成收获。" : ""),
        onConfirm: function () {
          executeClear(plot);
        }
      });
      return;
    }
    openPlotActionDialog(plotId);
  }

  function setActive(flag) {
    state.active = !!flag;
    if (state.active) {
      var uid = currentUserId();
      if (uid <= 0) {
        if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.switchModule)) {
          window.FarmHomeBridge.switchModule("user-select");
        }
        $.messager.show({
          title: "提示",
          msg: "请先选择用户，再进入农场。",
          timeout: motion().actionFeedbackMs,
          showType: "slide"
        });
        return;
      }
      $("#farmPanel").addClass("is-active is-module-enter");
      window.setTimeout(function () {
        $("#farmPanel").removeClass("is-module-enter");
      }, motion().moduleEnterMs);
      ensureSeedVisuals();
      switchTool(state.selectedTool || "inspect");
      loadOverviewByUser(uid, false);
      if (window.FarmWsBridge && window.FarmWsBridge.isConnected()) {
        onRealtimeStatus("connected");
      } else {
        onRealtimeStatus(state.wsStatus || "idle");
      }
      return;
    }
    $("#farmPanel").removeClass("is-active");
    stopPolling();
    stopGrowthTicker();
  }

  function bindRealtime() {
    $(document)
      .off("farm:realtime:overview.farm")
      .on("farm:realtime:overview.farm", function (_, payload) {
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

    $(document)
      .off("farm:realtime:status.farm")
      .on("farm:realtime:status.farm", function (_, text) {
        onRealtimeStatus(text);
      });
  }

  function bindEvents() {
    $("#farmFallbackSwitch")
      .off("change.farm")
      .on("change.farm", function () {
        fallbackEnabled();
        if ($(this).prop("checked")) {
          startPolling();
        } else {
          stopPolling();
        }
      });

    $("#farmToolBar")
      .off("click.farm", ".farm-tool-btn[data-tool]")
      .on("click.farm", ".farm-tool-btn[data-tool]", function () {
        switchTool($(this).attr("data-tool"));
        playSound("click");
      });

    $("#farmExpandBtn")
      .off("click.farm")
      .on("click.farm", function () {
        executeExpand();
        playSound("click");
      });

    $("#farmIsoContainer")
      .off("click.farm", ".farm-plot")
      .on("click.farm", ".farm-plot", function () {
        var plotId = $(this).attr("data-plot-id");
        applyToolOnPlot(plotId);
      });
  }

FarmModule.setActive = setActive;   FarmModule.loadOverviewByUser = loadOverviewByUser;   FarmModule.updateOverview = updateOverview;    window.FarmModule = FarmModule;   if (     window.FarmCore &&     $.isFunction(window.FarmCore.registerSetActiveModule)   ) {     window.FarmCore.registerSetActiveModule("farm", FarmModule, {       refresh: function () {         loadOverviewByUser(currentUserId(), false);       },     });   }    $(function () {     bindRealtime();     bindEvents();     renderAll({ plots: [] });     onRealtimeStatus("idle");   }); })(window, window.jQuery); 




