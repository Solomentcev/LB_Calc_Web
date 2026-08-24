package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.LCService;
import com.lb_calc_web.service.ProjectService;
import com.lb_calc_web.service.util.SizeValidator;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectController extends BaseCatalogController{
    private final ProjectService projectService;
    private final ALSService alsService;
    private final LBService lbService;
    private final LCService lcService;


    public ProjectController(ProjectService projectService, ALSService aLSService, LBService lbService, LCService lcService) {
        this.projectService = projectService;
        this.alsService = aLSService;
        this.lbService = lbService;
        this.lcService = lcService;

    }
    @GetMapping("/create")
    public String createProject(Model model) {
        ProjectDTO project = projectService.createProject();
        model.addAttribute("project", project);
        return "/projects/project";
    }
    @PostMapping("/{id}/save")
    public String saveProject( @ModelAttribute("project") ProjectDTO project, Model model) {
        for(ALSDTO als:project.getAlsList()){
            als=alsService.resizeLC(als);
            als=alsService.resizeLBs(als);
        }
        List<List<List<List<String>>>>  errorProjectList= SizeValidator.getErrorValidateProjectSizeList(project);
        if (errorProjectList.isEmpty()) {
            project=projectService.saveProject(project);
        }else{
            model.addAttribute("projectErrors", errorProjectList);
            model.addAttribute("project", project);
            return "/projects/project";
        }

        return "redirect:/projects/"+project.getId();
    }
    @PostMapping("/{id}/addALS" )
    public String addALS(@PathVariable(value = "id") Long projectId, Model model) {
        ProjectDTO projectDTO = projectService.addNewALSandSaveProject(projectId);
        model.addAttribute("project",projectDTO);

        return "redirect:/projects/"+projectId;
    }
    @GetMapping("/{id}/alss/{alsId}/delete" )
    public String deleteALS(@PathVariable(value = "id") Long id,
                            @PathVariable(value = "alsId") Long alsId,
                            Model model) {
        ProjectDTO projectDTO=projectService.deleteALSandSaveProject(id, alsId);
        model.addAttribute("project", projectDTO);
        return "redirect:/projects/"+id;
    }
    @GetMapping("/{id}")
    public String editProject(@PathVariable(value = "id") Long id, Model model) {
        ProjectDTO project =projectService.findById(id);
        model.addAttribute("project", project);
        return "projects/project";
    }
    @PostMapping("/{projectId}/alss/{alsId}/save")
    public String saveALSatProject(
            @PathVariable(value = "projectId") Long projectId,
            @PathVariable(value = "alsId") Long alsId,
            @ModelAttribute("als") ALSDTO als,
            Model model) {
        ProjectDTO project = projectService.findById(projectId);
        model.addAttribute("project", project);
        List<String> errorALSList=SizeValidator.getErrorValidateALSSizesList(als);
        als = alsService.resizeLC(als);
        List<String> errorLCList= SizeValidator.getErrorValidateLCSizesList(als.getLC());
        als = alsService.resizeLBs(als);
        List<List<String>> errorLBLists=SizeValidator.getErrorValidateLBSizesLists(als);
        if (errorALSList.isEmpty() && errorLCList.isEmpty() && errorLBLists.isEmpty()) {
           als=projectService.replaceALSandSaveProject(project,als,alsId);
        }
        else {
            model.addAttribute("ALSErrors", errorALSList);
            model.addAttribute("LCErrors", errorLCList);
            model.addAttribute("LBErrors", errorLBLists);
            model.addAttribute("als", als);
            return "projects/project_als";
        }
        return "redirect:/projects/"+projectId+"/alss/"+als.getId();
    }

    @GetMapping("/{projectId}/alss/{alsId}" )
    public String editALSatProject(@PathVariable(value = "projectId") Long projectId,
                                   @PathVariable(value = "alsId") Long alsId,
                                   Model model) {
        ProjectDTO project =projectService.findById(projectId);
        model.addAttribute("project", project);
        ALSDTO als = alsService.findById(alsId);
        model.addAttribute("als", als);
        return "projects/project_als";
    }
    @GetMapping("/{projectId}/alss/{alsId}/lcs/{lcId}")
    public String editLCatProject(@PathVariable(value = "projectId") Long projectId,
                                   @PathVariable(value = "alsId") Long alsId,
                                   @PathVariable(value = "lcId") Long lcId,
                                   Model model) {
        ProjectDTO project = projectService.findById(projectId);
        ALSDTO als =alsService.findById(alsId);
        LCDTO lc = lcService.findById(lcId);
        model.addAttribute("project", project);
        model.addAttribute("als", als);
        model.addAttribute("lc", lc);
        return "projects/project_lc";
    }
    @PostMapping("/{projectId}/alss/{alsId}/lcs/{lcId}/save")
    public String saveLCatProject(
            @PathVariable(value = "projectId") Long projectId,
            @PathVariable(value = "alsId") Long alsId,
            @PathVariable(value = "lcId") Long lcId,
            @ModelAttribute("lc") LCDTO lc,
            Model model) {
        ProjectDTO project =projectService.findById(projectId);
        ALSDTO als = alsService.findById(alsId);
        List<String> errorList =SizeValidator.getErrorValidateLCSizesList(lc);
        if (errorList.isEmpty()) {
            als=projectService.replaceLCandSaveProject(project,als,alsId,lc);
        }
        else {
            model.addAttribute("lc", lc);
            model.addAttribute("errors", errorList);
            return "projects/project_lc";
        }
        model.addAttribute("project", project);
        model.addAttribute("als", als);

        return "redirect:/projects/"+projectId+"/alss/"+ als.getId()+"/lcs/"+als.getLC().getId();
    }
    @PostMapping("/{projectId}/alss/{alsId}/addLB" )
    public String addLBatProject(@PathVariable(value = "projectId") Long projectId,
                                 @PathVariable(value = "alsId") Long alsId,
                                 Model model) {
        ALSDTO als=projectService.addLBAtProject(projectId,alsId);
        model.addAttribute("als", als);
        return "redirect:/projects/"+projectId+"/alss/"+als.getId();
    }
    @GetMapping("/{projectId}/alss/{alsId}/lbs/{lbId}/delete" )
    public String deleteLBatProject(@PathVariable(value = "projectId") Long projectId,
                                    @PathVariable(value = "alsId") Long alsId,
                                    @PathVariable(value = "lbId") Long lbId,
                                    Model model) {
        ALSDTO als=projectService.deleteLBatProject(projectId, alsId, lbId);
        model.addAttribute("als", als);
        return "redirect:/projects/"+projectId+"/alss/"+als.getId();
    }
    @GetMapping("/{projectId}/alss/{alsId}/lbs/{lbId}")
    private String editLBatProject(@PathVariable(value = "projectId") Long projectId,
                                   @PathVariable(value = "alsId") Long alsId,
                                   @PathVariable(value = "lbId") Long lbId,
                                   Model model) {
        ProjectDTO project = projectService.findById(projectId);
        ALSDTO als = alsService.findById(alsId);
        LBDTO lb =lbService.findById(lbId);
        model.addAttribute("project", project);
        model.addAttribute("als", als);
        model.addAttribute("lb", lb);

        return "projects/project_lb";
    }

    @PostMapping("/{projectId}/alss/{alsId}/lbs/{lbId}/save")
    public String saveLBatProject(@PathVariable(value = "projectId") Long projectId,
                                  @PathVariable(value = "alsId") Long alsId,
                                  @PathVariable(value = "lbId") Long lbId,
                                  @ModelAttribute("lb") LBDTO lb,
                                  Model model) {
        ProjectDTO project =projectService.findById(projectId);
        model.addAttribute("project", project);
        List<Object> ALSlbIdList;
        int newLbId;
        ALSDTO als = alsService.findById(alsId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);
        model.addAttribute("als", als);
        List<String> errorList= SizeValidator.getErrorValidateLBSizesList(lb);
        if (errorList.isEmpty()) {
             ALSlbIdList = projectService.saveLBatProject(projectId,alsId,lbId,lb);
             als=(ALSDTO) ALSlbIdList.get(0);
             newLbId = (int) ALSlbIdList.get(1);

        } else {
            model.addAttribute("errors",errorList);
            model.addAttribute("lb", lb);
            return "projects/project_lb";
        }

        return "redirect:/projects/"+projectId+"/alss/"+als.getId()+"/lbs/"+newLbId;
    }
    @GetMapping("/{id}/savetoexcel")
    private ResponseEntity<Resource> saveProjectToExcel(@PathVariable(value = "id") Long id) {
        ProjectDTO project = projectService.findById(id);
        String filename = project.getName()+".xlsx";
        InputStreamResource file = new InputStreamResource(projectService.exportToExcel(project));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

}
