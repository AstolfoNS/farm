(function (window, $) {
    var FarmSeedAdminModule = {};
    var Admin = window.FarmAdmin || {};
    var state = {
        active: false,
        initialized: false,
        bound: false,
        stageGeometrySyncing: false,
        seedRows: [],
        stageRows: [],
        currentSeedId: 0,
        currentSeedName: "",
        currentStageId: 0,
        seedQualityOptions: [],
        soilOptions: [],
        growthStageOptions: []
    };
    var POSITION_CANVAS_DEFAULT_WIDTH = 320;
    var POSITION_CANVAS_DEFAULT_HEIGHT = 410;
    var PREVIEW_CANVAS_DEFAULT_WIDTH = 220;
    var PREVIEW_CANVAS_DEFAULT_HEIGHT = 282;

    function motion() {
        if (window.FarmUi && $.isFunction(window.FarmUi.motion)) {
            return window.FarmUi.motion();
        }
        if ($.isFunction(window.farmMotion)) {
            return window.farmMotion();
        }
        return {moduleEnterMs: 260, actionFeedbackMs: 1200};
    }

    function asNumber(value, def) {
        if ($.isFunction(Admin.asNumber)) {
            return Admin.asNumber(value, def);
        }
        if (window.FarmUi && $.isFunction(window.FarmUi.asNumber)) {
            return window.FarmUi.asNumber(value, def);
        }
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function escapeHtml(text) {
        if (window.FarmUi && $.isFunction(window.FarmUi.escapeHtml)) {
            return window.FarmUi.escapeHtml(text);
        }
        return $("<div/>").text(text == null ? "" : String(text)).html();
    }

    function buildFileAccessUrl(relativePath) {
        if ($.isFunction(Admin.buildFileAccessUrl)) {
            return Admin.buildFileAccessUrl(relativePath);
        }
        var rel = $.trim(relativePath || "").replace(/^\/+/, "");
        var prefix = $.trim(window.FARM_FILE_PUBLIC_PREFIX || "/oss");
        if (prefix.charAt(0) !== "/") {
            prefix = "/" + prefix;
        }
        return rel ? ((prefix.replace(/\/+$/, "") || "/oss") + "/" + rel) : "";
    }

    function showMessage(msg, title) {
        if ($.isFunction(Admin.toast)) {
            Admin.toast(msg || "操作成功", title || "消息");
            return;
        }
        $.messager.show({
            title: title || "消息",
            msg: msg || "操作成功",
            timeout: motion().actionFeedbackMs,
            showType: "slide"
        });
    }

    function alertMessage(msg) {
        if ($.isFunction(Admin.alertError)) {
            Admin.alertError(msg || "操作失败");
            return;
        }
        $.messager.alert("提示", msg || "操作失败");
    }

    function boolOk(res) {
        if ($.isFunction(Admin.boolOk)) {
            return Admin.boolOk(res);
        }
        return window.FarmApi && $.isFunction(FarmApi.isOk) ? FarmApi.isOk(res) : false;
    }

    function listFromPageData(data) {
        if ($.isFunction(Admin.listFromPageData)) {
            return Admin.listFromPageData(data);
        }
        if (window.FarmGrid && $.isFunction(window.FarmGrid.listFromPageData)) {
            return window.FarmGrid.listFromPageData(data);
        }
        if (!data) {
            return [];
        }
        if ($.isArray(data.records)) {
            return data.records;
        }
        if ($.isArray(data.rows)) {
            return data.rows;
        }
        if ($.isArray(data.list)) {
            return data.list;
        }
        return [];
    }

    function getSeedTypeById(seedTypeId) {
        var targetId = asNumber(seedTypeId, 0);
        var matched = null;
        $.each(state.seedRows, function (_, row) {
            if (asNumber(row.id, 0) === targetId) {
                matched = row;
                return false;
            }
            return true;
        });
        return matched;
    }

    function loadDictionaries(done) {
        var count = 0;
        var target = 3;
        function finish() {
            count += 1;
            if (count >= target && $.isFunction(done)) {
                done();
            }
        }

        FarmApi.listSeedQualityOptions(function (res) {
            state.seedQualityOptions = boolOk(res) && $.isArray(res.data) ? res.data : [];
            $("#seedTypeQualityId").combobox("loadData", state.seedQualityOptions);
            finish();
        }, finish);

        FarmApi.listSoilOptions(function (res) {
            state.soilOptions = boolOk(res) && $.isArray(res.data) ? res.data : [];
            $("#seedTypeSoilIds").combobox("loadData", state.soilOptions);
            finish();
        }, finish);

        FarmApi.listGrowthStageOptions(function (res) {
            state.growthStageOptions = boolOk(res) && $.isArray(res.data) ? res.data : [];
            $("#seedStageGrowthStageId").combobox("loadData", state.growthStageOptions);
            finish();
        }, finish);
    }

    function buildSeedTypePagePayload(param) {
        return {
            name: $("#seedAdminName").textbox("getValue"),
            page: asNumber(param.page, 1),
            rows: asNumber(param.rows, 8),
            sort: param.sort || "id",
            order: param.order || "asc"
        };
    }

    function renderSeedStageManageBtn(index) {
        return "<a href='javascript:void(0)' class='seed-admin-stage-open' data-index='" + index + "'>阶段管理</a>";
    }

    function renderCover(value) {
        var src = $.trim(value || "");
        if (!src) {
            src = defaultSeedCover();
        }
        return "<img class='seed-admin-cover-thumb' src='" + escapeHtml(src) + "' alt='cover'>";
    }

    function setLinkButtonEnabled(selector, enabled) {
        try {
            $(selector).linkbutton(enabled ? "enable" : "disable");
        } catch (ignoreLinkbuttonError) {}
    }

    function syncActionButtons() {
        var hasSeed = state.currentSeedId > 0;
        var hasStage = hasSeed && state.currentStageId > 0;
        setLinkButtonEnabled("#seedAdminEditBtn", hasSeed);
        setLinkButtonEnabled("#seedAdminDeleteBtn", hasSeed);
        setLinkButtonEnabled("#seedAdminStageAddBtn", hasSeed);
        setLinkButtonEnabled("#seedAdminStageEditBtn", hasStage);
        setLinkButtonEnabled("#seedAdminStageDeleteBtn", hasStage);
        setLinkButtonEnabled("#seedAdminStageSaveListBtn", hasSeed);
    }

    function previewSeedTypeCover(url) {
        var src = $.trim(url || "");
        if (!src) {
            src = defaultSeedCover();
        }
        $("#seedTypeCoverPreview").attr("src", src);
    }

    function initTypeGrid() {
        var loader = (window.FarmGrid && $.isFunction(window.FarmGrid.buildRemoteLoader))
            ? window.FarmGrid.buildRemoteLoader({
                request: function (param, onSuccess, onError) {
                    FarmApi.seedTypePage(buildSeedTypePagePayload(param), onSuccess, onError);
                },
                resolve: function (res) {
                    if (!boolOk(res) || !res.data) {
                        return {total: 0, rows: []};
                    }
                    var rows = listFromPageData(res.data);
                    return {
                        total: asNumber(res.data.total, rows.length),
                        rows: rows
                    };
                },
                onRows: function (rows) {
                    state.seedRows = rows;
                }
            })
            : null;

        var options = {
            pagination: true,
            pageSize: 8,
            pageList: [8, 12, 20],
            idField: "id",
            loader: loader || function (param, success, error) {
                FarmApi.seedTypePage(buildSeedTypePagePayload(param), function (res) {
                    if (!boolOk(res) || !res.data) {
                        success({total: 0, rows: []});
                        return;
                    }
                    var rows = listFromPageData(res.data);
                    state.seedRows = rows;
                    success({
                        total: asNumber(res.data.total, rows.length),
                        rows: rows
                    });
                }, function () {
                    if ($.isFunction(error)) {
                        error.apply(this, arguments);
                    }
                });
            },
            columns: [[
                {field: "id", title: "ID", width: 42, sortable: true},
                {field: "coverImageUrl", title: "封面", width: 72, formatter: function (value) { return renderCover(value); }},
                {field: "name", title: "种子名称", width: 90, sortable: true},
                {field: "seedQualityName", title: "品质", width: 70},
                {field: "level", title: "等级", width: 45, sortable: true},
                {field: "harvestStageIndex", title: "收获阶段", width: 58},
                {field: "regrowStageIndex", title: "再生阶段", width: 58},
                {field: "unlockExperienceRequired", title: "解锁经验", width: 72, sortable: true},
                {field: "enableSoilTypeNames", title: "可种土壤", width: 120},
                {field: "price", title: "价格", width: 56, sortable: true},
                {field: "fruitPrice", title: "果实单价", width: 72, sortable: true},
                {field: "harvestExperience", title: "收获经验", width: 70},
                {field: "harvestScore", title: "收获积分", width: 70},
                {field: "totalGrowSeconds", title: "总成长(s)", width: 70},
                {
                    field: "action",
                    title: "阶段管理",
                    width: 70,
                    formatter: function (value, row, index) {
                        return renderSeedStageManageBtn(index);
                    }
                }
            ]],
            onSelect: function (index, row) {
                selectSeedType(row);
            },
            onDblClickRow: function () {
                openSeedTypeEditor("edit");
            },
            onLoadSuccess: function () {
                var rows = $("#seedAdminTypeGrid").datagrid("getRows") || [];
                if (rows.length <= 0) {
                    state.currentSeedId = 0;
                    state.currentSeedName = "";
                    state.currentStageId = 0;
                    $("#seedAdminStageHint").text("请先在上方选择一个种子，再进行阶段管理。");
                    syncActionButtons();
                } else {
                    var selectedIndex = 0;
                    if (state.currentSeedId > 0) {
                        $.each(rows, function (i, row) {
                            if (asNumber(row && row.id, 0) === state.currentSeedId) {
                                selectedIndex = i;
                                return false;
                            }
                            return true;
                        });
                    }
                    $("#seedAdminTypeGrid").datagrid("selectRow", selectedIndex);
                }

                var clickHandler = function ($trigger) {
                    var index = asNumber($trigger.attr("data-index"), -1);
                    if (index < 0) {
                        return;
                    }
                    $("#seedAdminTypeGrid").datagrid("selectRow", index);
                    var row = $("#seedAdminTypeGrid").datagrid("getRows")[index];
                    if (!row) {
                        return;
                    }
                    selectSeedType(row);
                    focusStagePanel();
                };

                if (window.FarmGrid && $.isFunction(window.FarmGrid.bindAction)) {
                    window.FarmGrid.bindAction("#seedAdminPanel", ".seed-admin-stage-open", clickHandler);
                    return;
                }
                $("#seedAdminPanel .seed-admin-stage-open").off("click").on("click", function () {
                    clickHandler($(this));
                });
            }
        };

        if (window.FarmGrid && $.isFunction(window.FarmGrid.init)) {
            window.FarmGrid.init("#seedAdminTypeGrid", options);
            return;
        }
        $("#seedAdminTypeGrid").datagrid(options);
    }

    function initStageGrid() {
        var loader = (window.FarmGrid && $.isFunction(window.FarmGrid.buildRemoteLoader))
            ? window.FarmGrid.buildRemoteLoader({
                request: function (param, onSuccess, onError) {
                    if (state.currentSeedId <= 0) {
                        onSuccess({code: 200, data: {records: [], total: 0}});
                        return;
                    }
                    FarmApi.seedStagePage({seedTypeId: state.currentSeedId}, onSuccess, onError);
                },
                resolve: function (res) {
                    if (!boolOk(res) || !res.data) {
                        return {total: 0, rows: []};
                    }
                    var rows = listFromPageData(res.data);
                    return {
                        total: asNumber(res.data.total, rows.length),
                        rows: rows
                    };
                },
                onRows: function (rows) {
                    state.stageRows = rows;
                }
            })
            : null;

        var options = {
            idField: "id",
            loader: loader || function (param, success, error) {
                if (state.currentSeedId <= 0) {
                    state.stageRows = [];
                    success({total: 0, rows: []});
                    return;
                }
                FarmApi.seedStagePage({seedTypeId: state.currentSeedId}, function (res) {
                    if (!boolOk(res) || !res.data) {
                        state.stageRows = [];
                        success({total: 0, rows: []});
                        return;
                    }
                    var rows = listFromPageData(res.data);
                    state.stageRows = rows;
                    success({
                        total: asNumber(res.data.total, rows.length),
                        rows: rows
                    });
                }, function () {
                    if ($.isFunction(error)) {
                        error.apply(this, arguments);
                    }
                });
            },
            columns: [[
                {field: "id", title: "ID", width: 40},
                {field: "stageIndex", title: "阶段序号", width: 56},
                {field: "growthStageName", title: "阶段标题", width: 84},
                {field: "durationSeconds", title: "成长时长(s)", width: 70},
                {field: "bugProbability", title: "虫害概率", width: 64},
                {field: "width", title: "宽", width: 40},
                {field: "height", title: "高", width: 40},
                {field: "offsetX", title: "offsetX", width: 56},
                {field: "offsetY", title: "offsetY", width: 56},
                {field: "assetUrl", title: "资源URL", width: 180}
            ]],
            onSelect: function (index, row) {
                state.currentStageId = asNumber(row && row.id, 0);
                syncActionButtons();
            },
            onDblClickRow: function () {
                openSeedStageEditor("edit");
            },
            onLoadSuccess: function () {
                var rows = $("#seedAdminStageGrid").datagrid("getRows") || [];
                if (rows.length <= 0) {
                    state.currentStageId = 0;
                    syncActionButtons();
                    return;
                }
                var selectedIndex = 0;
                if (state.currentStageId > 0) {
                    $.each(rows, function (i, row) {
                        if (asNumber(row && row.id, 0) === state.currentStageId) {
                            selectedIndex = i;
                            return false;
                        }
                        return true;
                    });
                }
                $("#seedAdminStageGrid").datagrid("selectRow", selectedIndex);
            }
        };

        if (window.FarmGrid && $.isFunction(window.FarmGrid.init)) {
            window.FarmGrid.init("#seedAdminStageGrid", options);
            return;
        }
        $("#seedAdminStageGrid").datagrid(options);
    }

    function refreshTypeGrid(toFirstPage) {
        if (window.FarmGrid && $.isFunction(window.FarmGrid.reload)) {
            window.FarmGrid.reload("#seedAdminTypeGrid", toFirstPage);
            return;
        }
        if (toFirstPage) {
            $("#seedAdminTypeGrid").datagrid("load", {page: 1});
            return;
        }
        $("#seedAdminTypeGrid").datagrid("reload");
    }

    function refreshStageGrid() {
        if (window.FarmGrid && $.isFunction(window.FarmGrid.reload)) {
            window.FarmGrid.reload("#seedAdminStageGrid", false);
            return;
        }
        $("#seedAdminStageGrid").datagrid("reload");
    }

    function selectSeedType(row) {
        state.currentSeedId = asNumber(row && row.id, 0);
        state.currentSeedName = row && row.name ? row.name : "";
        state.currentStageId = 0;
        if (state.currentSeedId <= 0) {
            $("#seedAdminStageHint").text("请先在上方选择一个种子，再进行阶段管理。");
            syncActionButtons();
            refreshStageGrid();
            return;
        }
        $("#seedAdminStageHint").text("当前种子: " + state.currentSeedName + " (ID: " + state.currentSeedId + ")");
        syncActionButtons();
        refreshStageGrid();
    }

    function focusStagePanel() {
        var $panel = $("#seedAdminStageToolbar").closest(".seed-admin-grid-panel");
        if ($panel.length > 0) {
            $panel.attr("tabindex", "-1").focus();
        }
        showMessage("已切换到“" + (state.currentSeedName || "当前种子") + "”的成长阶段清单");
    }

    function soilIdsByBits(bits) {
        var valueBits = asNumber(bits, 0);
        var ids = [];
        $.each(state.soilOptions, function (_, item) {
            var bit = asNumber(item && item.bitCode, 0);
            if (bit > 0 && (valueBits & bit) === bit) {
                ids.push(String(item.id));
            }
        });
        return ids;
    }

    function previewImageFromUrl(assetUrl) {
        var src = resolveStagePreviewSrc(assetUrl);
        $("#seedStagePreviewImage").attr("src", src);
        syncPreviewGeometryFromForm();
    }

    function resolveStagePreviewSrc(assetUrl) {
        var src = normalizeAssetUrl(assetUrl);
        var fallback = defaultSeedStage();
        if (!src) {
            return fallback;
        }
        if (src === fallback) {
            return fallback;
        }
        return src;
    }

    function normalizeAssetUrl(value) {
        var raw = $.trim(value || "");
        if (!raw) {
            return "";
        }
        if (/^(https?:)?\/\//i.test(raw) || /^data:/i.test(raw) || /^blob:/i.test(raw)) {
            return raw;
        }
        if (raw.charAt(0) === "/") {
            return raw;
        }
        if (raw.indexOf("resources/") === 0 || raw.indexOf("oss/") === 0) {
            return "/" + raw;
        }
        return buildFileAccessUrl(raw);
    }

    function setStageNumberValue(fieldName, value) {
        var fieldIdMap = {
            stageIndex: "#seedStageStageIndex",
            durationSeconds: "#seedStageDurationSeconds",
            bugProbability: "#seedStageBugProbability",
            width: "#seedStageWidth",
            height: "#seedStageHeight",
            offsetX: "#seedStageOffsetX",
            offsetY: "#seedStageOffsetY"
        };
        var selector = fieldIdMap[fieldName] || ("#seedStageEditorForm input[name='" + fieldName + "']");
        var $field = $(selector).first();
        if ($field.length <= 0) {
            return;
        }
        try {
            if (!$field.data("numberbox")) {
                $field.numberbox();
            }
            $field.numberbox("setValue", value);
            return;
        } catch (ignoreNumberboxSetError) {}
        $field.val(value);
    }

    function getStageNumberValue(fieldName, def) {
        var fieldIdMap = {
            stageIndex: "#seedStageStageIndex",
            durationSeconds: "#seedStageDurationSeconds",
            bugProbability: "#seedStageBugProbability",
            width: "#seedStageWidth",
            height: "#seedStageHeight",
            offsetX: "#seedStageOffsetX",
            offsetY: "#seedStageOffsetY"
        };
        var selector = fieldIdMap[fieldName] || ("#seedStageEditorForm input[name='" + fieldName + "']");
        var $field = $(selector).first();
        if ($field.length <= 0) {
            return asNumber(def, 0);
        }
        try {
            if (!$field.data("numberbox")) {
                $field.numberbox();
            }
            return asNumber($field.numberbox("getValue"), def);
        } catch (ignoreNumberboxGetError) {
            return asNumber($field.val(), def);
        }
    }

    function setTextboxValue($field, value) {
        if (!$field || $field.length <= 0) {
            return;
        }
        if ($.isFunction(Admin.setTextboxValue)) {
            Admin.setTextboxValue($field, value);
            return;
        }
        var safe = value == null ? "" : String(value);
        try {
            if (!$field.data("textbox")) {
                $field.textbox();
            }
            $field.textbox("setValue", safe);
            return;
        } catch (ignoreTextboxSetError) {}
        $field.val(safe);
    }

    function getTextboxValue($field, def) {
        if (!$field || $field.length <= 0) {
            return def || "";
        }
        if ($.isFunction(Admin.getTextboxValue)) {
            return Admin.getTextboxValue($field, def);
        }
        try {
            if (!$field.data("textbox")) {
                $field.textbox();
            }
            return $.trim($field.textbox("getValue") || (def || ""));
        } catch (ignoreTextboxGetError) {
            return $.trim($field.val() || (def || ""));
        }
    }

    function getNumberboxValue($field, def) {
        if (!$field || $field.length <= 0) {
            return asNumber(def, 0);
        }
        if ($.isFunction(Admin.getNumberboxValue)) {
            return Admin.getNumberboxValue($field, def);
        }
        try {
            if (!$field.data("numberbox")) {
                $field.numberbox();
            }
            return asNumber($field.numberbox("getValue"), def);
        } catch (ignoreNumberboxGetError) {
            return asNumber($field.val(), def);
        }
    }

    function stageGeometryFromForm() {
        return {
            width: getStageNumberValue("width", 100),
            height: getStageNumberValue("height", 120),
            left: getStageNumberValue("offsetX", 110),
            top: getStageNumberValue("offsetY", 280)
        };
    }

    function getPreviewGeometryScale() {
        var previewWidth = $("#seedStagePreviewBox").innerWidth() || PREVIEW_CANVAS_DEFAULT_WIDTH;
        var previewHeight = $("#seedStagePreviewBox").innerHeight() || PREVIEW_CANVAS_DEFAULT_HEIGHT;
        var positionWidth = $("#seedStagePositionCanvas").innerWidth() || POSITION_CANVAS_DEFAULT_WIDTH;
        var positionHeight = $("#seedStagePositionCanvas").innerHeight() || POSITION_CANVAS_DEFAULT_HEIGHT;
        return {
            x: previewWidth / positionWidth,
            y: previewHeight / positionHeight
        };
    }

    function applyGeometryToPreview(geometry) {
        var scale = getPreviewGeometryScale();
        $("#seedStagePreviewImage").css({
            width: Math.round(geometry.width * scale.x) + "px",
            height: Math.round(geometry.height * scale.y) + "px",
            left: Math.round(geometry.left * scale.x) + "px",
            top: Math.round(geometry.top * scale.y) + "px"
        });
    }

    function applyGeometryToPositionImage(geometry) {
        $("#seedStagePositionImage").css({
            width: geometry.width + "px",
            height: geometry.height + "px",
            left: geometry.left + "px",
            top: geometry.top + "px"
        });
    }

    function syncPreviewGeometryFromForm() {
        applyGeometryToPreview(stageGeometryFromForm());
    }

    function syncPositionImageFromForm() {
        applyGeometryToPositionImage(stageGeometryFromForm());
    }

    function syncFormFromPositionImage() {
        var $image = $("#seedStagePositionImage");
        state.stageGeometrySyncing = true;
        setStageNumberValue("width", $image.outerWidth() || 0);
        setStageNumberValue("height", $image.outerHeight() || 0);
        setStageNumberValue("offsetX", parseInt($image.css("left"), 10) || 0);
        setStageNumberValue("offsetY", parseInt($image.css("top"), 10) || 0);
        state.stageGeometrySyncing = false;
    }

    function syncFormOffsetFromPositionImage() {
        var $image = $("#seedStagePositionImage");
        state.stageGeometrySyncing = true;
        setStageNumberValue("offsetX", parseInt($image.css("left"), 10) || 0);
        setStageNumberValue("offsetY", parseInt($image.css("top"), 10) || 0);
        state.stageGeometrySyncing = false;
    }

    function bindStageGeometrySyncEvents() {
        var fieldIdMap = {
            width: "#seedStageWidth",
            height: "#seedStageHeight",
            offsetX: "#seedStageOffsetX",
            offsetY: "#seedStageOffsetY"
        };
        $.each(["width", "height", "offsetX", "offsetY"], function (_, name) {
            var $field = $(fieldIdMap[name] || ("#seedStageEditorForm input[name='" + name + "']")).first();
            try {
                var $textbox = $field.numberbox("textbox");
                if (!$textbox || $textbox.length <= 0) {
                    return;
                }
                $textbox.off(".stageSync").on("input.stageSync blur.stageSync keyup.stageSync", function () {
                    if (state.stageGeometrySyncing) {
                        return;
                    }
                    syncPreviewGeometryFromForm();
                    if ($("#seedStagePositionDialog").dialog("options").closed === false) {
                        syncPositionImageFromForm();
                    }
                });
            } catch (ignoreBindError) {}
        });
    }

    function bindSearchEnterEvent() {
        try {
            var $searchInput = $("#seedAdminName").textbox("textbox");
            $searchInput.off("keydown.seedAdmin").on("keydown.seedAdmin", function (event) {
                if (event.keyCode === 13) {
                    refreshTypeGrid(true);
                }
            });
        } catch (ignoreSearchBindError) {}
    }

    function bindSeedTypeCoverUrlWatcher() {
        try {
            var $coverInput = $("#seedTypeCoverImageUrl").textbox("textbox");
            $coverInput.off("change.seedAdmin blur.seedAdmin input.seedAdmin")
                .on("change.seedAdmin blur.seedAdmin input.seedAdmin", function () {
                    previewSeedTypeCover(getTextboxValue($("#seedTypeCoverImageUrl"), ""));
                });
        } catch (ignoreCoverBindError) {}
    }

    function bindSeedStageAssetUrlWatcher() {
        try {
            var $assetInput = $("#seedStageAssetUrl").textbox("textbox");
            $assetInput.off(".seedAdminAssetUrl")
                .on("change.seedAdminAssetUrl blur.seedAdminAssetUrl input.seedAdminAssetUrl", function () {
                    var val = normalizeAssetUrl(getTextboxValue($("#seedStageAssetUrl"), ""));
                    if (val) {
                        setTextboxValue($("#seedStageAssetUrl"), val);
                    }
                    previewImageFromUrl(val);
                });
        } catch (ignoreAssetBindError) {}
    }

    function bindUploadPickerEvents() {
        if ($.isFunction(Admin.bindUploadPicker)) {
            Admin.bindUploadPicker({
                namespace: ".seedAdminUploadTypeCover",
                buttonSelector: "#seedTypeUploadCoverBtn",
                fileSelector: "#seedTypeCoverFile",
                category: "seed-cover",
                onSuccess: function (url, payload, raw) {
                    setTextboxValue($("#seedTypeCoverImageUrl"), url);
                    previewSeedTypeCover(url);
                    showMessage((raw && raw.msg) || "上传成功");
                },
                onError: function (msg) {
                    alertMessage(msg || "上传失败，请稍后重试");
                }
            });
            Admin.bindUploadPicker({
                namespace: ".seedAdminUploadStage",
                buttonSelector: "#seedStageUploadImageBtn",
                fileSelector: "#seedStageImageFile",
                category: "seed-stage",
                onSuccess: function (url, payload, raw) {
                    var normalized = normalizeAssetUrl(url);
                    setTextboxValue($("#seedStageAssetUrl"), normalized);
                    previewImageFromUrl(normalized);
                    showMessage((raw && raw.msg) || "上传成功");
                },
                onError: function (msg) {
                    alertMessage(msg || "上传失败，请稍后重试");
                }
            });
            return;
        }

        $(document)
            .off("click.seedAdminUpload", "#seedTypeUploadCoverBtn")
            .on("click.seedAdminUpload", "#seedTypeUploadCoverBtn", function () {
                var $file = $("#seedTypeCoverFile");
                var input = $file.get(0);
                if (!input) {
                    return;
                }
                input.value = "";
                input.click();
            });

        $(document)
            .off("click.seedAdminUpload", "#seedStageUploadImageBtn")
            .on("click.seedAdminUpload", "#seedStageUploadImageBtn", function () {
                var $file = $("#seedStageImageFile");
                var input = $file.get(0);
                if (!input) {
                    return;
                }
                input.value = "";
                input.click();
            });

        $(document)
            .off("change.seedAdminUpload", "#seedTypeCoverFile")
            .on("change.seedAdminUpload", "#seedTypeCoverFile", function () {
                uploadFile($(this), "seed-cover", function (url) {
                    setTextboxValue($("#seedTypeCoverImageUrl"), url);
                    previewSeedTypeCover(url);
                });
            });

        $(document)
            .off("change.seedAdminUpload", "#seedStageImageFile")
            .on("change.seedAdminUpload", "#seedStageImageFile", function () {
                uploadFile($(this), "seed-stage", function (url) {
                    var normalized = normalizeAssetUrl(url);
                    setTextboxValue($("#seedStageAssetUrl"), normalized);
                    previewImageFromUrl(normalized);
                });
            });
    }

    function fillSeedTypeForm(row) {
        $("#seedTypeEditorForm").form("clear");
        $("#seedTypeEditorForm input[name='id']").val(0);
        $("#seedTypeQualityId").combobox("setValue", "");
        $("#seedTypeSoilIds").combobox("clear");
        previewSeedTypeCover("");
        if (!row) {
            if (state.seedQualityOptions.length > 0) {
                $("#seedTypeQualityId").combobox("setValue", state.seedQualityOptions[0].id);
            }
            return;
        }

        $("#seedTypeEditorForm").form("load", {
            name: row.name || "",
            level: asNumber(row.level, 1),
            harvestStageIndex: row.harvestStageIndex == null ? "" : row.harvestStageIndex,
            regrowStageIndex: row.regrowStageIndex == null ? "" : row.regrowStageIndex,
            unlockExperienceRequired: asNumber(row.unlockExperienceRequired, 0),
            price: asNumber(row.price, 0),
            fruitPrice: asNumber(row.fruitPrice, 0),
            harvestExperience: asNumber(row.harvestExperience, 0),
            harvestScore: asNumber(row.harvestScore, 0),
            harvestFruitNumber: asNumber(row.harvestFruitNumber, 0),
            fruitLossPerBug: asNumber(row.fruitLossPerBug, 0),
            maxBugLimit: asNumber(row.maxBugLimit, 0),
            maxHarvestCount: asNumber(row.maxHarvestCount, 1),
            bugKillExperienceReward: asNumber(row.bugKillExperienceReward, 0),
            bugKillScoreReward: asNumber(row.bugKillScoreReward, 0),
            bugKillCoinReward: asNumber(row.bugKillCoinReward, 0),
            coverImageUrl: row.coverImageUrl || "",
            description: row.description || ""
        });
        $("#seedTypeEditorForm input[name='id']").val(asNumber(row.id, 0));
        $("#seedTypeQualityId").combobox("setValue", asNumber(row.seedQualityId, 0));
        $("#seedTypeSoilIds").combobox("setValues", soilIdsByBits(row.enableSoilTypeBits));
        previewSeedTypeCover(row.coverImageUrl || "");
    }

    function refreshSeedTypeEditorLayout() {
        window.setTimeout(function () {
            var $soil = $("#seedTypeSoilIds");
            var $desc = $("#seedTypeEditorForm input[name='description']");
            var $coverUrl = $("#seedTypeCoverImageUrl");
            var fullWidth = Math.max(560, $(".seed-type-editor-table .seed-admin-full-field").first().innerWidth() || 560);
            var coverWrapWidth = Math.max(360, $(".seed-type-cover-input-wrap").innerWidth() || (fullWidth - 90));
            var coverInputWidth = Math.max(260, coverWrapWidth - 90);
            try { $soil.combobox("resize", fullWidth); } catch (ignoreSoilResizeError) {}
            try { $desc.textbox("resize", fullWidth); } catch (ignoreDescResizeError) {}
            try { $coverUrl.textbox("resize", coverInputWidth); } catch (ignoreCoverResizeError) {}
        }, 0);
    }

    function refreshSeedStageEditorLayout() {
        window.setTimeout(function () {
            var $table = $(".seed-stage-editor-table");
            var fullWidth = Math.max(560, $table.find(".seed-admin-full-field").first().innerWidth() || 560);
            var editorWidth = Math.max(220, Math.min(236, Math.floor((($table.innerWidth() || 760) - 320) / 2)));
            var assetInputWidth = Math.max(300, fullWidth - 96);
            var stageFieldSelectors = [
                "#seedStageStageIndex",
                "#seedStageGrowthStageId",
                "#seedStageDurationSeconds",
                "#seedStageBugProbability",
                "#seedStageWidth",
                "#seedStageHeight",
                "#seedStageOffsetX",
                "#seedStageOffsetY"
            ];
            $.each(stageFieldSelectors, function (_, selector) {
                var $field = $(selector);
                if ($field.length <= 0) {
                    return;
                }
                try {
                    if ($field.hasClass("easyui-combobox")) {
                        $field.combobox("resize", editorWidth);
                        return;
                    }
                    $field.numberbox("resize", editorWidth);
                } catch (ignoreStageFieldResizeError) {}
            });
            try { $("#seedStageAssetUrl").textbox("resize", assetInputWidth); } catch (ignoreAssetResizeError) {}
            syncPreviewGeometryFromForm();
        }, 0);
    }

    function openSeedTypeEditor(mode) {
        if (mode === "edit") {
            var row = $("#seedAdminTypeGrid").datagrid("getSelected");
            if (!row) {
                alertMessage("请先选择要编辑的种子类型");
                return;
            }
            fillSeedTypeForm(row);
            $("#seedTypeEditorDialog").dialog("setTitle", "编辑种子类型").dialog("open");
            refreshSeedTypeEditorLayout();
            return;
        }
        fillSeedTypeForm(null);
        $("#seedTypeEditorDialog").dialog("setTitle", "新增种子类型").dialog("open");
        refreshSeedTypeEditorLayout();
    }

    function saveSeedType() {
        if (!$("#seedTypeEditorForm").form("validate")) {
            alertMessage("请先完善种子必填信息");
            return;
        }
        var $form = $("#seedTypeEditorForm");
        var id = asNumber($form.find("input[name='id']").val(), 0);
        var soilIds = [];
        var seedQualityId = 0;
        try {
            soilIds = $("#seedTypeSoilIds").combobox("getValues") || [];
        } catch (ignoreSoilGetError) {}
        try {
            seedQualityId = asNumber($("#seedTypeQualityId").combobox("getValue"), 0);
        } catch (ignoreQualityGetError) {}
        var payload = {
            id: id > 0 ? id : null,
            name: getTextboxValue($form.find("input[name='name']").first(), ""),
            seedQualityId: seedQualityId,
            soilTypeIds: (soilIds || []).join(","),
            level: getNumberboxValue($form.find("input[name='level']").first(), 1),
            harvestStageIndex: getNumberboxValue($form.find("input[name='harvestStageIndex']").first(), 0) || null,
            unlockExperienceRequired: getNumberboxValue($form.find("input[name='unlockExperienceRequired']").first(), 0),
            regrowStageIndex: getNumberboxValue($form.find("input[name='regrowStageIndex']").first(), 0) || null,
            price: getNumberboxValue($form.find("input[name='price']").first(), 0),
            fruitPrice: getNumberboxValue($form.find("input[name='fruitPrice']").first(), 0),
            harvestExperience: getNumberboxValue($form.find("input[name='harvestExperience']").first(), 0),
            harvestScore: getNumberboxValue($form.find("input[name='harvestScore']").first(), 0),
            harvestFruitNumber: getNumberboxValue($form.find("input[name='harvestFruitNumber']").first(), 0),
            fruitLossPerBug: getNumberboxValue($form.find("input[name='fruitLossPerBug']").first(), 0),
            maxBugLimit: getNumberboxValue($form.find("input[name='maxBugLimit']").first(), 0),
            maxHarvestCount: getNumberboxValue($form.find("input[name='maxHarvestCount']").first(), 1),
            bugKillExperienceReward: getNumberboxValue($form.find("input[name='bugKillExperienceReward']").first(), 0),
            bugKillScoreReward: getNumberboxValue($form.find("input[name='bugKillScoreReward']").first(), 0),
            bugKillCoinReward: getNumberboxValue($form.find("input[name='bugKillCoinReward']").first(), 0),
            coverImageUrl: getTextboxValue($("#seedTypeCoverImageUrl"), ""),
            description: getTextboxValue($form.find("input[name='description']").first(), "")
        };
        FarmApi.seedTypeSave(payload, function (res) {
            if (!boolOk(res)) {
                alertMessage((res && res.msg) || "保存失败");
                return;
            }
            showMessage((res && res.msg) || "保存成功");
            $("#seedTypeEditorDialog").dialog("close");
            refreshTypeGrid(id <= 0);
        }, function () {
            alertMessage("保存失败，请稍后重试");
        });
    }

    function deleteSeedType() {
        var row = $("#seedAdminTypeGrid").datagrid("getSelected");
        if (!row) {
            alertMessage("请先选择要删除的种子类型");
            return;
        }
        if ($.isFunction(Admin.confirm)) {
            Admin.confirm("确认删除种子类型【" + (row.name || "") + "】吗？", doDeleteSeedType);
            return;
        }
        $.messager.confirm("确认", "确认删除种子类型【" + (row.name || "") + "】吗？", function (ok) {
            if (ok) {
                doDeleteSeedType();
            }
        });

        function doDeleteSeedType() {
            FarmApi.seedTypeDelete({id: asNumber(row.id, 0)}, function (res) {
                if (!boolOk(res)) {
                    alertMessage((res && res.msg) || "删除失败");
                    return;
                }
                showMessage((res && res.msg) || "删除成功");
                if (state.currentSeedId === asNumber(row.id, 0)) {
                    state.currentSeedId = 0;
                    state.currentSeedName = "";
                    $("#seedAdminStageHint").text("请先在上方选择一个种子，再进行阶段管理。");
                }
                refreshTypeGrid(false);
                refreshStageGrid();
            }, function () {
                alertMessage("删除失败，请稍后重试");
            });
        }
    }

    function fillSeedStageForm(row) {
        $("#seedStageEditorForm").form("clear");
        $("#seedStageEditorForm input[name='id']").val(0);
        $("#seedStageEditorForm input[name='seedTypeId']").val(state.currentSeedId);
        setStageNumberValue("width", 100);
        setStageNumberValue("height", 120);
        setStageNumberValue("offsetX", 110);
        setStageNumberValue("offsetY", 280);
        setStageNumberValue("durationSeconds", 30);
        setStageNumberValue("bugProbability", 0);
        setStageNumberValue("stageIndex", asNumber(state.stageRows.length, 0) + 1);
        previewImageFromUrl("");
        syncPreviewGeometryFromForm();
        if (!row) {
            return;
        }
        $("#seedStageEditorForm").form("load", {
            stageIndex: asNumber(row.stageIndex, 1),
            growthStageId: asNumber(row.growthStageId, 0),
            durationSeconds: asNumber(row.durationSeconds, 0),
            bugProbability: row.bugProbability == null ? 0 : row.bugProbability,
            width: asNumber(row.width, 100),
            height: asNumber(row.height, 120),
            offsetX: asNumber(row.offsetX, 110),
            offsetY: asNumber(row.offsetY, 280),
            assetUrl: row.assetUrl || ""
        });
        $("#seedStageEditorForm input[name='id']").val(asNumber(row.id, 0));
        $("#seedStageEditorForm input[name='seedTypeId']").val(asNumber(row.seedTypeId, state.currentSeedId));
        previewImageFromUrl(row.assetUrl || "");
        syncPreviewGeometryFromForm();
    }

    function openSeedStageEditor(mode) {
        if (state.currentSeedId <= 0) {
            alertMessage("请先选择种子类型");
            return;
        }
        if (mode === "edit") {
            var row = $("#seedAdminStageGrid").datagrid("getSelected");
            if (!row) {
                alertMessage("请先选择要编辑的阶段");
                return;
            }
            fillSeedStageForm(row);
            $("#seedStageEditorDialog").dialog("setTitle", "编辑成长阶段").dialog("open");
            refreshSeedStageEditorLayout();
            bindStageGeometrySyncEvents();
            return;
        }
        fillSeedStageForm(null);
        $("#seedStageEditorDialog").dialog("setTitle", "新增成长阶段").dialog("open");
        refreshSeedStageEditorLayout();
        bindStageGeometrySyncEvents();
    }

    function saveSeedStage() {
        if (state.currentSeedId <= 0) {
            alertMessage("请先选择种子类型");
            return;
        }
        if (!$("#seedStageEditorForm").form("validate")) {
            alertMessage("请先完善阶段信息");
            return;
        }
        var normalizedAssetUrl = normalizeAssetUrl(getTextboxValue($("#seedStageAssetUrl"), ""));
        setTextboxValue($("#seedStageAssetUrl"), normalizedAssetUrl);
        var payload = {
            id: asNumber($("#seedStageEditorForm input[name='id']").val(), 0) || null,
            seedTypeId: state.currentSeedId,
            growthStageId: asNumber($("#seedStageGrowthStageId").combobox("getValue"), 0),
            stageIndex: getStageNumberValue("stageIndex", 1),
            durationSeconds: getStageNumberValue("durationSeconds", 0),
            bugProbability: Number(getStageNumberValue("bugProbability", 0)),
            width: getStageNumberValue("width", 100),
            height: getStageNumberValue("height", 120),
            offsetX: getStageNumberValue("offsetX", 110),
            offsetY: getStageNumberValue("offsetY", 280),
            assetUrl: normalizedAssetUrl
        };
        FarmApi.seedStageSave(payload, function (res) {
            if (!boolOk(res)) {
                alertMessage((res && res.msg) || "阶段保存失败");
                return;
            }
            showMessage((res && res.msg) || "阶段保存成功");
            $("#seedStageEditorDialog").dialog("close");
            refreshStageGrid();
            refreshTypeGrid(false);
        }, function () {
            alertMessage("阶段保存失败，请稍后重试");
        });
    }

    function deleteSeedStage() {
        if (state.currentSeedId <= 0) {
            alertMessage("请先选择种子类型");
            return;
        }
        var row = $("#seedAdminStageGrid").datagrid("getSelected");
        if (!row) {
            alertMessage("请先选择要删除的阶段");
            return;
        }
        if ($.isFunction(Admin.confirm)) {
            Admin.confirm("确认删除阶段 #" + asNumber(row.stageIndex, 0) + " 吗？", doDeleteSeedStage);
            return;
        }
        $.messager.confirm("确认", "确认删除阶段 #" + asNumber(row.stageIndex, 0) + " 吗？", function (ok) {
            if (ok) {
                doDeleteSeedStage();
            }
        });

        function doDeleteSeedStage() {
            FarmApi.seedStageDelete({id: asNumber(row.id, 0)}, function (res) {
                if (!boolOk(res)) {
                    alertMessage((res && res.msg) || "删除失败");
                    return;
                }
                showMessage((res && res.msg) || "删除成功");
                refreshStageGrid();
                refreshTypeGrid(false);
            }, function () {
                alertMessage("删除失败，请稍后重试");
            });
        }
    }

    function saveSeedStageList() {
        if (state.currentSeedId <= 0) {
            alertMessage("请先选择种子类型");
            return;
        }
        FarmApi.seedStageValidate({id: state.currentSeedId}, function (res) {
            if (!boolOk(res)) {
                showMessage((res && res.msg) || "阶段清单校验失败", "校验失败");
                return;
            }
            showMessage((res && res.msg) || "阶段清单保存成功");
            refreshStageGrid();
            refreshTypeGrid(false);
        }, function (xhr) {
            showMessage(resolveAjaxErrorMessage(xhr, "阶段清单校验失败"), "校验失败");
        });
    }

    function resolveAjaxErrorMessage(xhr, fallback) {
        var response = xhr && xhr.responseJSON;
        if (response && response.msg) {
            return response.msg;
        }
        var raw = xhr && xhr.responseText ? $.trim(xhr.responseText) : "";
        if (!raw) {
            return fallback || "操作失败";
        }
        try {
            response = JSON.parse(raw);
            if (response && response.msg) {
                return response.msg;
            }
        } catch (ignoreParseError) {}
        return fallback || raw;
    }

    function uploadFile($fileInput, category, onSuccess) {
        if ($.isFunction(Admin.uploadFile)) {
            Admin.uploadFile({
                fileInput: $fileInput,
                category: category,
                onSuccess: function (url, payload, raw) {
                    var normalizedUrl = normalizeAssetUrl(url);
                    if ($.isFunction(onSuccess)) {
                        onSuccess(normalizedUrl, payload, raw);
                    }
                    showMessage((raw && raw.msg) || "上传成功");
                },
                onError: function (msg) {
                    alertMessage(msg || "上传失败，请稍后重试");
                }
            });
            return;
        }
        var files = $fileInput.prop("files");
        if (!files || files.length <= 0) {
            return;
        }
        var formData = new FormData();
        formData.append("file", files[0]);
        formData.append("category", category);
        $.ajax({
            url: "/file/upload",
            type: "post",
            data: formData,
            processData: false,
            contentType: false,
            dataType: "json",
            success: function (res) {
                if (!boolOk(res) || !res.data) {
                    alertMessage((res && res.msg) || "上传失败");
                    return;
                }
                var url = $.trim(res.data.accessUrl || res.data.path || "");
                if (!url) {
                    var rel = $.trim(res.data.relativePath || "");
                    if (rel) {
                        url = buildFileAccessUrl(rel);
                    }
                }
                url = normalizeAssetUrl(url);
                if (!url) {
                    alertMessage("上传成功但未返回访问地址，请稍后重试");
                    return;
                }
                if ($.isFunction(onSuccess)) {
                    onSuccess(url, res.data);
                }
                showMessage((res && res.msg) || "上传成功");
            },
            error: function () {
                alertMessage("上传失败，请稍后重试");
            },
            complete: function () {
                $fileInput.val("");
            }
        });
    }

    function openPositionEditor() {
        var assetUrl = normalizeAssetUrl(getTextboxValue($("#seedStageAssetUrl"), ""));
        if (!$.trim(assetUrl)) {
            assetUrl = defaultSeedStage();
        }
        setTextboxValue($("#seedStageAssetUrl"), assetUrl);
        var $image = $("#seedStagePositionImage");
        $image.attr("src", resolveStagePreviewSrc(assetUrl));
        syncPositionImageFromForm();
        try {
            $image.draggable("destroy");
        } catch (ignoreDragDestroy) {}
        try {
            $image.resizable("destroy");
        } catch (ignoreResizeDestroy) {}
        $image.draggable({
            containment: "#seedStagePositionCanvas",
            onDrag: function () {
                syncFormOffsetFromPositionImage();
                syncPreviewGeometryFromForm();
            }
        });
        $("#seedStagePositionDialog").dialog("open");
    }

    function applyPositionEditor() {
        syncFormFromPositionImage();
        $("#seedStagePositionDialog").dialog("close");
        syncPreviewGeometryFromForm();
    }

    function reportInitError(stage, err) {
        var detail = err && err.message ? err.message : err;
        window.__seedAdminInitError = stage + ": " + detail;
        if (window.console && $.isFunction(window.console.error)) {
            window.console.error("[seed-admin] " + stage, err);
        }
    }

    function tryBindEventsWithRetry(attempt, maxAttempts) {
        var current = asNumber(attempt, 1);
        var max = asNumber(maxAttempts, 18);
        try {
            bindEvents();
            state.bound = true;
            return;
        } catch (bindError) {
            reportInitError("bindEvents#" + current, bindError);
        }
        if (current >= max) {
            return;
        }
        window.setTimeout(function () {
            if (!state.bound) {
                tryBindEventsWithRetry(current + 1, max);
            }
        }, 80);
    }

    function bindEvents() {
        $("#seedAdminSearchBtn").off("click.seedAdmin").on("click.seedAdmin", function () {
            refreshTypeGrid(true);
        });
        bindSearchEnterEvent();
        $("#seedAdminResetBtn").off("click.seedAdmin").on("click.seedAdmin", function () {
            $("#seedAdminName").textbox("setValue", "");
            refreshTypeGrid(true);
        });
        $("#seedAdminAddBtn").off("click.seedAdmin").on("click.seedAdmin", function () {
            openSeedTypeEditor("add");
        });
        $("#seedAdminEditBtn").off("click.seedAdmin").on("click.seedAdmin", function () {
            openSeedTypeEditor("edit");
        });
        $("#seedAdminDeleteBtn").off("click.seedAdmin").on("click.seedAdmin", deleteSeedType);

        $("#seedAdminStageAddBtn").off("click.seedAdmin").on("click.seedAdmin", function () {
            openSeedStageEditor("add");
        });
        $("#seedAdminStageEditBtn").off("click.seedAdmin").on("click.seedAdmin", function () {
            openSeedStageEditor("edit");
        });
        $("#seedAdminStageDeleteBtn").off("click.seedAdmin").on("click.seedAdmin", deleteSeedStage);
        $("#seedAdminStageSaveListBtn").off("click.seedAdmin").on("click.seedAdmin", saveSeedStageList);

        $("#seedTypeSaveBtn").off("click.seedAdmin").on("click.seedAdmin", saveSeedType);
        $("#seedTypeCancelBtn").off("click.seedAdmin").on("click.seedAdmin", function () {
            $("#seedTypeEditorDialog").dialog("close");
        });
        $("#seedStageSaveBtn").off("click.seedAdmin").on("click.seedAdmin", saveSeedStage);
        $("#seedStageCancelBtn").off("click.seedAdmin").on("click.seedAdmin", function () {
            $("#seedStageEditorDialog").dialog("close");
        });

        bindUploadPickerEvents();

        $("#seedStagePositionEditorBtn").off("click.seedAdmin").on("click.seedAdmin", openPositionEditor);
        $("#seedStagePositionApplyBtn").off("click.seedAdmin").on("click.seedAdmin", applyPositionEditor);
        $("#seedStagePositionCancelBtn").off("click.seedAdmin").on("click.seedAdmin", function () {
            $("#seedStagePositionDialog").dialog("close");
        });

        bindSeedStageAssetUrlWatcher();
        bindSeedTypeCoverUrlWatcher();

        window.setTimeout(bindStageGeometrySyncEvents, 0);
        window.setTimeout(bindSearchEnterEvent, 80);
        syncActionButtons();
    }

    function initComboboxes() {
        $("#seedTypeQualityId").combobox({
            valueField: "id",
            textField: "text",
            editable: false,
            panelHeight: "auto"
        });
        $("#seedTypeSoilIds").combobox({
            valueField: "id",
            textField: "text",
            editable: false,
            panelHeight: 260,
            multiple: true,
            multivalue: true,
            separator: ","
        });
        $("#seedStageGrowthStageId").combobox({
            valueField: "id",
            textField: "text",
            editable: false,
            panelHeight: "auto"
        });
    }

    function initDialogs() {
        $("#seedTypeEditorDialog").dialog({
            cls: "farm-dialog-window seed-admin-dialog-window",
            onOpen: function () {
                refreshSeedTypeEditorLayout();
                bindSeedTypeCoverUrlWatcher();
            },
            onResize: refreshSeedTypeEditorLayout
        });
        $("#seedStageEditorDialog").dialog({
            cls: "farm-dialog-window seed-admin-dialog-window",
            onOpen: function () {
                refreshSeedStageEditorLayout();
                bindSeedStageAssetUrlWatcher();
                bindStageGeometrySyncEvents();
            },
            onResize: refreshSeedStageEditorLayout
        });
        $("#seedStagePositionDialog").dialog({cls: "farm-dialog-window seed-admin-dialog-window"});
    }

    function init() {
        if (state.initialized) {
            return;
        }
        initComboboxes();
        initTypeGrid();
        initStageGrid();
        initDialogs();
        loadDictionaries(function () {
            refreshTypeGrid(true);
        });
        state.initialized = true;
    }

    function setActive(flag) {
        state.active = !!flag;
        if (!state.initialized) {
            try {
                init();
            } catch (initError) {
                reportInitError("init", initError);
            }
        }
        if (!state.bound) {
            tryBindEventsWithRetry(1, 18);
        }
        if (!state.initialized) {
            return;
        }

        if (state.active) {
            if (window.FarmUi && $.isFunction(window.FarmUi.showPanel)) {
                window.FarmUi.showPanel($("#seedAdminPanel"));
            } else {
                $("#seedAdminPanel").stop(true, true).css("display", "none").fadeIn(motion().moduleEnterMs);
            }
            window.setTimeout(function () {
                $("#seedAdminTypeGrid").datagrid("resize");
                $("#seedAdminStageGrid").datagrid("resize");
                refreshTypeGrid(false);
                refreshStageGrid();
            }, motion().moduleEnterMs + 20);
            return;
        }

        $("#seedTypeEditorDialog").dialog("close");
        $("#seedStageEditorDialog").dialog("close");
        $("#seedStagePositionDialog").dialog("close");
        if (window.FarmUi && $.isFunction(window.FarmUi.hidePanel)) {
            window.FarmUi.hidePanel($("#seedAdminPanel"));
        } else {
            $("#seedAdminPanel").stop(true, true).fadeOut(motion().moduleEnterMs);
        }
    }

    FarmSeedAdminModule.setActive = setActive;
    FarmSeedAdminModule.refresh = function () {
        refreshTypeGrid(false);
        refreshStageGrid();
    };

    window.FarmSeedAdminModule = FarmSeedAdminModule;
    if (window.FarmCore && $.isFunction(window.FarmCore.registerSetActiveModule)) {
        window.FarmCore.registerSetActiveModule("seed-admin", FarmSeedAdminModule, {refreshMethod: "refresh"});
    }
})(window, window.jQuery);


    function defaultSeedCover() {
        return (window.farmDefaultAsset && window.farmDefaultAsset("seedCover")) || "";
    }

    function defaultSeedStage() {
        return (window.farmDefaultAsset && window.farmDefaultAsset("seedStage")) || "";
    }
