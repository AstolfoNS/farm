package cn.jxufe.farm.controller;

import cn.jxufe.farm.model.bean.EasyUIData;
import cn.jxufe.farm.model.bean.EasyUIDataPageRequest;
import cn.jxufe.farm.model.bean.Message;
import cn.jxufe.farm.service.SeedService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/seed")
public class SeedController {

    private final SeedService seedService;

    public SeedController(SeedService seedService) {
        this.seedService = seedService;
    }

    @RequestMapping(value = "/gridDataFilterSortPage", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EasyUIData gridDataFilterSortPageGet(@RequestParam(required = false, defaultValue = "") String name,
                                                EasyUIDataPageRequest pageRequest) {
        return seedService.gridDataFilterSortPage(name, pageRequest);
    }

    @PostMapping(value = "/gridDataFilterSortPage", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EasyUIData gridDataFilterSortPage(@RequestParam(required = false, defaultValue = "") String name,
                                             EasyUIDataPageRequest pageRequest) {
        return seedService.gridDataFilterSortPage(name, pageRequest);
    }

    @PostMapping(value = "/addOrUpdate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message addOrUpdate(@RequestParam Map<String, String> params) {
        return seedService.addOrUpdate(params);
    }

    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message delete(@RequestParam("id") Long id) {
        return seedService.delete(id);
    }

    @GetMapping(value = "/qualityOptions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> qualityOptions() {
        return seedService.qualityOptions();
    }

    @GetMapping(value = "/seedQualityOptions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> seedQualityOptions() {
        return seedService.qualityOptions();
    }

    @GetMapping(value = "/soilOptions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> soilOptions() {
        return seedService.soilOptions();
    }

    @GetMapping(value = "/soilTypeOptions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> soilTypeOptions() {
        return seedService.soilOptions();
    }

    @GetMapping(value = "/growthStageOptions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> growthStageOptions() {
        return seedService.growthStageOptions();
    }

    @GetMapping(value = "/stageOptions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> stageOptions() {
        return seedService.growthStageOptions();
    }

    @RequestMapping(value = "/stage/gridDataFilterSortPage", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EasyUIData stageGridDataFilterSortPageGet(@RequestParam("seedTypeId") Long seedTypeId) {
        return seedService.stageGridDataFilterSortPage(seedTypeId);
    }

    @PostMapping(value = "/stage/gridDataFilterSortPage", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EasyUIData stageGridDataFilterSortPage(@RequestParam("seedTypeId") Long seedTypeId) {
        return seedService.stageGridDataFilterSortPage(seedTypeId);
    }

    @PostMapping(value = "/stage/addOrUpdate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message stageAddOrUpdate(@RequestParam Map<String, String> params) {
        return seedService.stageAddOrUpdate(params);
    }

    @PostMapping(value = "/stage/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message stageDelete(@RequestParam("id") Long id) {
        return seedService.stageDelete(id);
    }
}
