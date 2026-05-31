(function (window, $) {
    var FarmPlotAdminModule = {};
    var state = {
        active: false,
        inited: false,
        tab: "soil",
        soilQuery: {page: 1, rows: 10, name: ""},
        typeQuery: {page: 1, rows: 10, name: ""},
        userQuery: {page: 1, rows: 10, username: ""}
    };

    var DEFAULT_SOIL_COVER = "/oss/defaults/soil/soil-default.png";
    var DEFAULT_PLOT_COVER = "/oss/defaults/plot/plot-cover-default.png";
    var DEFAULT_PLOT_ICON = "/oss/defaults/plot/plot-icon-default.png";

    function asNumber(value, def) {
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function boolOk(res) {
        return window.FarmApi && $.isFunction(window.FarmApi.isOk) && window.FarmApi.isOk(res);
    }

    function trimText(value) {
        return $.trim(value == null ? "" : String(value));
    }

    function escapeHtml(text) {
        return $("<div/>").text(text == null ? "" : String(text)).html();
    }

    function defaultText(value, fallback) {
        var safe = trimText(value);
        if (safe.length > 0) {
            return safe;
        }
        return fallback || "";
    }

    function showMessage(msg) {
        $.messager.show({title: "提示", msg: msg || "操作成功", timeout: 1300, showType: "slide"});
    }

    function alertMessage(msg) {
        $.messager.alert("提示", msg || "操作失败");
    }

    function setTextboxValue($el, value) {
        try {
            $el.textbox("setValue", value == null ? "" : value);
        } catch (ignoreTextboxSet) {
            $el.val(value == null ? "" : value);
        }
    }

    function getTextboxValue($el, fallback) {
        try {
            var v = $el.textbox("getValue");
            return trimText(v);
        } catch (ignoreTextboxGet) {
            return trimText($el.val()) || (fallback || "");
        }
    }

    function setNumberboxValue($el, value) {
        try {
            $el.numberbox("setValue", value == null ? "" : value);
        } catch (ignoreNumberSet) {
            $el.val(value == null ? "" : value);
        }
    }

    function getNumberboxValue($el, fallback) {
        try {
            return asNumber($el.numberbox("getValue"), fallback || 0);
        } catch (ignoreNumberGet) {
            return asNumber($el.val(), fallback || 0);
        }
    }

    function renderCover(src, fallback) {
        var safe = defaultText(src, fallback);
        return "<img src='" + escapeHtml(safe) + "' alt='' style='width:46px;height:46px;object-fit:cover;border:1px solid rgba(172,236,153,.64);background:rgba(0,62,0,.55)'>";
    }

    function loadFilterData(data) {
        if (data && data.records) {
            return data;
        }
        if (data && data.data && data.data.records) {
            return data.data;
        }
        return {total: 0, records: []};
    }

    function switchTab(tabName) {
        state.tab = tabName;
        $(".plot-admin-tab").removeClass("is-active");
        $(".plot-admin-tab[data-tab='" + tabName + "']").addClass("is-active");
        $(".plot-admin-pane").removeClass("is-active");
        if (tabName === "type") {
            $("#plotAdminTypePane").addClass("is-active");
            refreshTypeGrid();
            return;
        }
        if (tabName === "user") {
            $("#plotAdminUserPane").addClass("is-active");
            refreshUserGrid();
            return;
        }
        $("#plotAdminSoilPane").addClass("is-active");
        refreshSoilGrid();
    }

    function initTabs() {
        $(".plot-admin-tab").off("click.plotAdminTab").on("click.plotAdminTab", function () {
            var tab = $(this).attr("data-tab");
            switchTab(tab || "soil");
        });
    }

    function refreshSoilGrid() {
        $("#plotAdminSoilGrid").datagrid("load", {
            page: state.soilQuery.page,
            rows: state.soilQuery.rows,
            name: state.soilQuery.name,
            sort: "id",
            order: "asc"
        });
    }

    function refreshTypeGrid() {
        $("#plotAdminTypeGrid").datagrid("load", {
            page: state.typeQuery.page,
            rows: state.typeQuery.rows,
            name: state.typeQuery.name,
            sort: "id",
            order: "asc"
        });
    }

    function refreshUserGrid() {
        $("#plotAdminUserGrid").datagrid("load", {
            page: state.userQuery.page,
            rows: state.userQuery.rows,
            username: state.userQuery.username,
            sort: "userId",
            order: "asc"
        });
    }

    function initSoilGrid() {
        $("#plotAdminSoilGrid").datagrid({
            fit: true,
            border: false,
            singleSelect: true,
            pagination: true,
            rownumbers: true,
            loader: function (param, success, error) {
                var request = {
                    page: asNumber(param.page, state.soilQuery.page),
                    rows: asNumber(param.rows, state.soilQuery.rows),
                    name: trimText(param.name)
                };
                window.FarmApi.plotSoilPage(request, function (res) {
                    if (!boolOk(res) || !res.data) {
                        success({total: 0, records: []});
                        return;
                    }
                    success(res.data);
                }, function () {
                    if ($.isFunction(error)) {
                        error.apply(this, arguments);
                    }
                });
            },
            queryParams: {
                page: state.soilQuery.page,
                rows: state.soilQuery.rows,
                name: state.soilQuery.name
            },
            columns: [[
                {field: "id", title: "ID", width: 56},
                {field: "name", title: "土壤名称", width: 110},
                {field: "bitCode", title: "bitCode", width: 78},
                {field: "level", title: "等级", width: 66},
                {field: "unlockExperienceRequired", title: "解锁经验", width: 94},
                {field: "growSpeedMultiplier", title: "成长倍率", width: 86},
                {field: "coverImageUrl", title: "图片", width: 62, formatter: function (value) { return renderCover(value, DEFAULT_SOIL_COVER); }},
                {field: "description", title: "描述", width: 240}
            ]],
            onLoadSuccess: function () {
                var pager = $("#plotAdminSoilGrid").datagrid("getPager");
                pager.pagination({
                    onSelectPage: function (pageNo, pageSize) {
                        state.soilQuery.page = pageNo;
                        state.soilQuery.rows = pageSize;
                        refreshSoilGrid();
                    }
                });
            }
        });
    }

    function initTypeGrid() {
        $("#plotAdminTypeGrid").datagrid({
            fit: true,
            border: false,
            singleSelect: true,
            pagination: true,
            rownumbers: true,
            loader: function (param, success, error) {
                var request = {
                    page: asNumber(param.page, state.typeQuery.page),
                    rows: asNumber(param.rows, state.typeQuery.rows),
                    name: trimText(param.name)
                };
                window.FarmApi.plotTypePage(request, function (res) {
                    if (!boolOk(res) || !res.data) {
                        success({total: 0, records: []});
                        return;
                    }
                    success(res.data);
                }, function () {
                    if ($.isFunction(error)) {
                        error.apply(this, arguments);
                    }
                });
            },
            queryParams: {
                page: state.typeQuery.page,
                rows: state.typeQuery.rows,
                name: state.typeQuery.name
            },
            columns: [[
                {field: "id", title: "ID", width: 56},
                {field: "name", title: "地块类型", width: 110},
                {field: "soilTypeName", title: "关联土壤", width: 98},
                {field: "unlockRequired", title: "需解锁", width: 70, formatter: function (v) { return v ? "是" : "否"; }},
                {field: "defaultUsable", title: "默认可用", width: 82, formatter: function (v) { return v ? "是" : "否"; }},
                {field: "defaultUnlockExperienceRequired", title: "默认经验", width: 92},
                {field: "sortOrder", title: "排序", width: 66},
                {field: "coverImageUrl", title: "封面", width: 62, formatter: function (value) { return renderCover(value, DEFAULT_PLOT_COVER); }},
                {field: "description", title: "描述", width: 220}
            ]],
            onLoadSuccess: function () {
                var pager = $("#plotAdminTypeGrid").datagrid("getPager");
                pager.pagination({
                    onSelectPage: function (pageNo, pageSize) {
                        state.typeQuery.page = pageNo;
                        state.typeQuery.rows = pageSize;
                        refreshTypeGrid();
                    }
                });
            }
        });
    }

    function initUserGrid() {
        $("#plotAdminUserGrid").datagrid({
            fit: true,
            border: false,
            singleSelect: true,
            pagination: true,
            rownumbers: true,
            loader: function (param, success, error) {
                var request = {
                    page: asNumber(param.page, state.userQuery.page),
                    rows: asNumber(param.rows, state.userQuery.rows),
                    username: trimText(param.username)
                };
                window.FarmApi.plotUserPage(request, function (res) {
                    if (!boolOk(res) || !res.data) {
                        success({total: 0, records: []});
                        return;
                    }
                    success(res.data);
                }, function () {
                    if ($.isFunction(error)) {
                        error.apply(this, arguments);
                    }
                });
            },
            queryParams: {
                page: state.userQuery.page,
                rows: state.userQuery.rows,
                username: state.userQuery.username
            },
            columns: [[
                {field: "userId", title: "用户ID", width: 76},
                {field: "username", title: "用户名", width: 110},
                {field: "nickname", title: "昵称", width: 110},
                {field: "currentTotalPlots", title: "当前总地块", width: 92},
                {field: "currentUnlockedPlots", title: "当前已解锁", width: 92},
                {field: "totalPlotCount", title: "配置总地块", width: 92},
                {field: "unlockedPlotCount", title: "配置已解锁", width: 96},
                {field: "defaultPlotTypeName", title: "默认地块类型", width: 112}
            ]],
            onLoadSuccess: function () {
                var pager = $("#plotAdminUserGrid").datagrid("getPager");
                pager.pagination({
                    onSelectPage: function (pageNo, pageSize) {
                        state.userQuery.page = pageNo;
                        state.userQuery.rows = pageSize;
                        refreshUserGrid();
                    }
                });
            }
        });
    }

    function loadSoilOptionsForTypeForm(selectedId) {
        window.FarmApi.plotSoilPage({page: 1, rows: 200, sort: "id", order: "asc"}, function (res) {
            var options = [];
            if (boolOk(res) && res.data && $.isArray(res.data.records)) {
                $.each(res.data.records, function (_, item) {
                    options.push({
                        id: asNumber(item.id, 0),
                        text: item.name || ("土壤#" + asNumber(item.id, 0))
                    });
                });
            }
            $("#plotTypeSoilTypeId").combobox({
                valueField: "id",
                textField: "text",
                editable: false,
                panelHeight: 180,
                data: options
            });
            if (asNumber(selectedId, 0) > 0) {
                $("#plotTypeSoilTypeId").combobox("setValue", asNumber(selectedId, 0));
            } else if (options.length > 0) {
                $("#plotTypeSoilTypeId").combobox("setValue", options[0].id);
            }
        }, function () {
            $("#plotTypeSoilTypeId").combobox({
                valueField: "id",
                textField: "text",
                editable: false,
                panelHeight: 80,
                data: []
            });
        });
    }

    function previewSoilCover(url) {
        $("#plotSoilCoverPreview").attr("src", defaultText(url, DEFAULT_SOIL_COVER));
    }

    function previewTypeCover(url) {
        $("#plotTypeCoverPreview").attr("src", defaultText(url, DEFAULT_PLOT_COVER));
    }

    function openSoilEditor(row) {
        var data = row || {};
        $("#plotSoilEditorForm").form("clear");
        setTextboxValue($("#plotSoilEditorForm input[name='id']"), asNumber(data.id, 0));
        setTextboxValue($("#plotSoilEditorForm input[name='name']"), data.name || "");
        setNumberboxValue($("#plotSoilEditorForm input[name='bitCode']"), asNumber(data.bitCode, 1));
        setNumberboxValue($("#plotSoilEditorForm input[name='level']"), asNumber(data.level, 1));
        setNumberboxValue($("#plotSoilEditorForm input[name='unlockExperienceRequired']"), asNumber(data.unlockExperienceRequired, 0));
        setTextboxValue($("#plotSoilEditorForm input[name='growSpeedMultiplier']"), data.growSpeedMultiplier || "1.00");
        setTextboxValue($("#plotSoilEditorForm input[name='coverImageUrl']"), data.coverImageUrl || DEFAULT_SOIL_COVER);
        setTextboxValue($("#plotSoilEditorForm input[name='description']"), data.description || "");
        previewSoilCover(data.coverImageUrl || DEFAULT_SOIL_COVER);
        $("#plotSoilEditorDialog").dialog("setTitle", data.id ? "编辑土壤类型" : "新增土壤类型").dialog("open");
    }

    function openTypeEditor(row) {
        var data = row || {};
        $("#plotTypeEditorForm").form("clear");
        setTextboxValue($("#plotTypeEditorForm input[name='id']"), asNumber(data.id, 0));
        setTextboxValue($("#plotTypeEditorForm input[name='name']"), data.name || "");
        setTextboxValue($("#plotTypeEditorForm input[name='iconUrl']"), data.iconUrl || DEFAULT_PLOT_ICON);
        setTextboxValue($("#plotTypeCoverImageUrl"), data.coverImageUrl || data.iconUrl || DEFAULT_PLOT_COVER);
        setNumberboxValue($("#plotTypeEditorForm input[name='defaultUnlockExperienceRequired']"), asNumber(data.defaultUnlockExperienceRequired, 0));
        setNumberboxValue($("#plotTypeEditorForm input[name='sortOrder']"), asNumber(data.sortOrder, 0));
        setTextboxValue($("#plotTypeEditorForm input[name='description']"), data.description || "");
        previewTypeCover(data.coverImageUrl || data.iconUrl || DEFAULT_PLOT_COVER);
        loadSoilOptionsForTypeForm(data.soilTypeId);
        try {
            if (data.unlockRequired === false) {
                $("#plotTypeUnlockRequired").switchbutton("uncheck");
            } else {
                $("#plotTypeUnlockRequired").switchbutton("check");
            }
            if (data.defaultUsable === false) {
                $("#plotTypeDefaultUsable").switchbutton("uncheck");
            } else {
                $("#plotTypeDefaultUsable").switchbutton("check");
            }
        } catch (ignoreSwitchSet) {}
        $("#plotTypeEditorDialog").dialog("setTitle", data.id ? "编辑地块类型" : "新增地块类型").dialog("open");
    }

    function uploadFile($fileInput, category, onSuccess) {
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
                var url = trimText(res.data.accessUrl || res.data.path || "");
                if (!url) {
                    var rel = trimText(res.data.relativePath || "");
                    if (rel) {
                        url = "/oss/" + rel.replace(/^\/+/, "");
                    }
                }
                if (!url) {
                    alertMessage("上传成功但未返回访问地址");
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

    function saveSoil() {
        if (!$("#plotSoilEditorForm").form("validate")) {
            return;
        }
        var payload = {
            id: asNumber(getTextboxValue($("#plotSoilEditorForm input[name='id']"), "0"), 0) || null,
            name: getTextboxValue($("#plotSoilEditorForm input[name='name']"), ""),
            bitCode: getNumberboxValue($("#plotSoilEditorForm input[name='bitCode']"), 1),
            level: getNumberboxValue($("#plotSoilEditorForm input[name='level']"), 1),
            unlockExperienceRequired: getNumberboxValue($("#plotSoilEditorForm input[name='unlockExperienceRequired']"), 0),
            growSpeedMultiplier: getTextboxValue($("#plotSoilEditorForm input[name='growSpeedMultiplier']"), "1.00"),
            coverImageUrl: getTextboxValue($("#plotSoilCoverImageUrl"), DEFAULT_SOIL_COVER),
            description: getTextboxValue($("#plotSoilEditorForm input[name='description']"), "")
        };
        window.FarmApi.plotSoilSave(payload, function (res) {
            if (!boolOk(res)) {
                alertMessage((res && res.msg) || "保存失败");
                return;
            }
            $("#plotSoilEditorDialog").dialog("close");
            showMessage((res && res.msg) || "保存成功");
            refreshSoilGrid();
        }, function () {
            alertMessage("保存失败，请稍后重试");
        });
    }

    function saveType() {
        if (!$("#plotTypeEditorForm").form("validate")) {
            return;
        }
        var unlockRequired = true;
        var defaultUsable = true;
        try {
            unlockRequired = !!$("#plotTypeUnlockRequired").switchbutton("options").checked;
            defaultUsable = !!$("#plotTypeDefaultUsable").switchbutton("options").checked;
        } catch (ignoreSwitchGet) {}

        var payload = {
            id: asNumber(getTextboxValue($("#plotTypeEditorForm input[name='id']"), "0"), 0) || null,
            name: getTextboxValue($("#plotTypeEditorForm input[name='name']"), ""),
            soilTypeId: asNumber($("#plotTypeSoilTypeId").combobox("getValue"), 0),
            iconUrl: getTextboxValue($("#plotTypeEditorForm input[name='iconUrl']"), DEFAULT_PLOT_ICON),
            coverImageUrl: getTextboxValue($("#plotTypeCoverImageUrl"), DEFAULT_PLOT_COVER),
            unlockRequired: unlockRequired,
            defaultUsable: defaultUsable,
            defaultUnlockExperienceRequired: getNumberboxValue($("#plotTypeEditorForm input[name='defaultUnlockExperienceRequired']"), 0),
            sortOrder: getNumberboxValue($("#plotTypeEditorForm input[name='sortOrder']"), 0),
            description: getTextboxValue($("#plotTypeEditorForm input[name='description']"), "")
        };
        if (payload.soilTypeId <= 0) {
            alertMessage("请先选择关联土壤");
            return;
        }
        window.FarmApi.plotTypeSave(payload, function (res) {
            if (!boolOk(res)) {
                alertMessage((res && res.msg) || "保存失败");
                return;
            }
            $("#plotTypeEditorDialog").dialog("close");
            showMessage((res && res.msg) || "保存成功");
            refreshTypeGrid();
        }, function () {
            alertMessage("保存失败，请稍后重试");
        });
    }

    function deleteSoil() {
        var row = $("#plotAdminSoilGrid").datagrid("getSelected");
        if (!row) {
            alertMessage("请先选择要删除的土壤类型");
            return;
        }
        $.messager.confirm("确认", "确认删除土壤类型【" + escapeHtml(row.name || "") + "】吗？", function (ok) {
            if (!ok) {
                return;
            }
            window.FarmApi.plotSoilDelete({id: asNumber(row.id, 0)}, function (res) {
                if (!boolOk(res)) {
                    alertMessage((res && res.msg) || "删除失败");
                    return;
                }
                showMessage((res && res.msg) || "删除成功");
                refreshSoilGrid();
            }, function () {
                alertMessage("删除失败，请稍后重试");
            });
        });
    }

    function deleteType() {
        var row = $("#plotAdminTypeGrid").datagrid("getSelected");
        if (!row) {
            alertMessage("请先选择要删除的地块类型");
            return;
        }
        $.messager.confirm("确认", "确认删除地块类型【" + escapeHtml(row.name || "") + "】吗？", function (ok) {
            if (!ok) {
                return;
            }
            window.FarmApi.plotTypeDelete({id: asNumber(row.id, 0)}, function (res) {
                if (!boolOk(res)) {
                    alertMessage((res && res.msg) || "删除失败");
                    return;
                }
                showMessage((res && res.msg) || "删除成功");
                refreshTypeGrid();
            }, function () {
                alertMessage("删除失败，请稍后重试");
            });
        });
    }

    function bindToolbarEvents() {
        $("#plotAdminSoilSearchBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            state.soilQuery.name = getTextboxValue($("#plotAdminSoilName"), "");
            state.soilQuery.page = 1;
            refreshSoilGrid();
        });
        $("#plotAdminSoilResetBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            setTextboxValue($("#plotAdminSoilName"), "");
            state.soilQuery.name = "";
            state.soilQuery.page = 1;
            refreshSoilGrid();
        });
        $("#plotAdminSoilAddBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            openSoilEditor(null);
        });
        $("#plotAdminSoilEditBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            var row = $("#plotAdminSoilGrid").datagrid("getSelected");
            if (!row) {
                alertMessage("请先选择要编辑的土壤类型");
                return;
            }
            openSoilEditor(row);
        });
        $("#plotAdminSoilDeleteBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            deleteSoil();
        });

        $("#plotAdminTypeSearchBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            state.typeQuery.name = getTextboxValue($("#plotAdminTypeName"), "");
            state.typeQuery.page = 1;
            refreshTypeGrid();
        });
        $("#plotAdminTypeResetBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            setTextboxValue($("#plotAdminTypeName"), "");
            state.typeQuery.name = "";
            state.typeQuery.page = 1;
            refreshTypeGrid();
        });
        $("#plotAdminTypeAddBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            openTypeEditor(null);
        });
        $("#plotAdminTypeEditBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            var row = $("#plotAdminTypeGrid").datagrid("getSelected");
            if (!row) {
                alertMessage("请先选择要编辑的地块类型");
                return;
            }
            openTypeEditor(row);
        });
        $("#plotAdminTypeDeleteBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            deleteType();
        });

        $("#plotAdminUserSearchBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            state.userQuery.username = getTextboxValue($("#plotAdminUserName"), "");
            state.userQuery.page = 1;
            refreshUserGrid();
        });
        $("#plotAdminUserResetBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            setTextboxValue($("#plotAdminUserName"), "");
            state.userQuery.username = "";
            state.userQuery.page = 1;
            refreshUserGrid();
        });

        $("#plotSoilSaveBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            saveSoil();
        });
        $("#plotSoilCancelBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            $("#plotSoilEditorDialog").dialog("close");
        });
        $("#plotTypeSaveBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            saveType();
        });
        $("#plotTypeCancelBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            $("#plotTypeEditorDialog").dialog("close");
        });

        $("#plotSoilUploadCoverBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            var $file = $("#plotSoilCoverFile");
            $file.val("");
            $file.trigger("click");
        });
        $("#plotTypeUploadCoverBtn").off("click.plotAdmin").on("click.plotAdmin", function () {
            var $file = $("#plotTypeCoverFile");
            $file.val("");
            $file.trigger("click");
        });

        $("#plotSoilCoverFile").off("change.plotAdmin").on("change.plotAdmin", function () {
            uploadFile($(this), "soil-cover", function (url) {
                setTextboxValue($("#plotSoilCoverImageUrl"), url);
                previewSoilCover(url);
            });
        });
        $("#plotTypeCoverFile").off("change.plotAdmin").on("change.plotAdmin", function () {
            uploadFile($(this), "plot-cover", function (url) {
                setTextboxValue($("#plotTypeCoverImageUrl"), url);
                previewTypeCover(url);
            });
        });

        $("#plotSoilCoverImageUrl").textbox("textbox").off("change.plotAdmin blur.plotAdmin input.plotAdmin")
            .on("change.plotAdmin blur.plotAdmin input.plotAdmin", function () {
                previewSoilCover(getTextboxValue($("#plotSoilCoverImageUrl"), DEFAULT_SOIL_COVER));
            });
        $("#plotTypeCoverImageUrl").textbox("textbox").off("change.plotAdmin blur.plotAdmin input.plotAdmin")
            .on("change.plotAdmin blur.plotAdmin input.plotAdmin", function () {
                previewTypeCover(getTextboxValue($("#plotTypeCoverImageUrl"), DEFAULT_PLOT_COVER));
            });
    }

    function initDialogs() {
        $("#plotSoilEditorDialog").dialog({
            cls: "farm-dialog-window plot-admin-dialog-window"
        });
        $("#plotTypeEditorDialog").dialog({
            cls: "farm-dialog-window plot-admin-dialog-window"
        });
        $("#plotTypeUnlockRequired").switchbutton();
        $("#plotTypeDefaultUsable").switchbutton();
    }

    function ensureInit() {
        if (state.inited) {
            return;
        }
        $("#plotAdminSoilName, #plotAdminTypeName, #plotAdminUserName").textbox();
        $(".plot-admin-toolbar .easyui-linkbutton").linkbutton();
        initTabs();
        initSoilGrid();
        initTypeGrid();
        initUserGrid();
        initDialogs();
        bindToolbarEvents();
        state.inited = true;
    }

    function setActive(flag) {
        state.active = !!flag;
        if (state.active) {
            ensureInit();
            if (window.FarmUi && $.isFunction(window.FarmUi.showPanel)) {
                window.FarmUi.showPanel($("#plotAdminPanel"));
            } else {
                $("#plotAdminPanel").stop(true, true).css("display", "none").fadeIn(window.farmMotion().moduleEnterMs);
            }
            switchTab(state.tab || "soil");
            return;
        }
        if (window.FarmUi && $.isFunction(window.FarmUi.hidePanel)) {
            window.FarmUi.hidePanel($("#plotAdminPanel"));
        } else {
            $("#plotAdminPanel").stop(true, true).fadeOut(window.farmMotion().moduleEnterMs);
        }
    }

    FarmPlotAdminModule.setActive = setActive;
    FarmPlotAdminModule.reload = function () {
        if (!state.active) {
            return;
        }
        switchTab(state.tab || "soil");
    };
    window.FarmPlotAdminModule = FarmPlotAdminModule;
    if (window.FarmCore && $.isFunction(window.FarmCore.registerSetActiveModule)) {
        window.FarmCore.registerSetActiveModule("plot-admin", FarmPlotAdminModule, {refreshMethod: "reload"});
    }
})(window, window.jQuery);
