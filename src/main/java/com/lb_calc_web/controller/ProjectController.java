package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.model.attributes.*;
import com.lb_calc_web.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/projects")
public class ProjectController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;
    private final ALSService alsService;
    private final LBService lbService;
    private final LCService lcService;
    private final List<Colors> colorsList;
    private final List<PositionLC> positionLCList;
    private final List<Payment> paymentList;
    private final List<DisplayLC> displayList;
    private final List<BarReader> barReaderList;
    private final List<TypeLb> typeLbList;
    private final List<DirectionDoorOpening> directionDoorOpeningList;

    public ProjectController(ProjectService projectService, ALSService aLSService, LBService lbService, LCService lcService) {
        this.projectService = projectService;
        this.alsService = aLSService;
        this.lbService = lbService;
        this.lcService = lcService;
        colorsList = Arrays.asList(Colors.values());
        positionLCList = Arrays.asList(PositionLC.values());
        displayList = Arrays.asList(DisplayLC.values());
        barReaderList = Arrays.asList(BarReader.values());
        typeLbList= Arrays.asList(TypeLb.values());
        directionDoorOpeningList = Arrays.asList(DirectionDoorOpening.values());
        paymentList = Arrays.asList(Payment.values());
    }
    @GetMapping("/create")
    private String createProject(Model model) {
        ProjectDTO project = projectService.createProject();
        model.addAttribute("project", project);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("positionLCList", positionLCList);
        model.addAttribute("paymentList", paymentList);
        model.addAttribute("displayList", displayList);
        model.addAttribute("barReaderList", barReaderList);
        model.addAttribute("typeList", typeLbList);
        return "/projects/project";
    }
    @PostMapping("/{id}/save")
    public String saveProject(@ModelAttribute("project") ProjectDTO project,Model model) {
        logger.debug("Saving Project...");
        for(ALSDTO als:project.getAlsList()){
            als=alsService.resizeLC(als);
            als=alsService.resizeLBs(als);
        }
        List<List<List<List<String>>>>  errorProjectList=SizeValidator.getErrorValidateProjectSizeList(project);
        if (errorProjectList.isEmpty()) {
            project=projectService.saveProject(project);
        }else{
            logger.warn("Saving Project Error");
            for(List<List<List<String>>> alsError: errorProjectList) {
                logger.warn("[Ошибки размеров АКХ]:"+ alsError.get(0));
                logger.warn("[Ошибки размеров МХ]:"+ alsError.get(1));
                logger.warn("[Ошибки размеров МУ]:"+ alsError.get(2));
            }
            model.addAttribute("projectErrors", errorProjectList);

            model.addAttribute("project", project);
            model.addAttribute("colorsList", colorsList);
            model.addAttribute("positionLCList", positionLCList);
            model.addAttribute("paymentList", paymentList);
            model.addAttribute("displayList", displayList);
            model.addAttribute("barReaderList", barReaderList);
            model.addAttribute("typeList", typeLbList);
            return "/projects/project";
        }

        return "redirect:/projects/"+project.getId();
    }
    @GetMapping("/{id}/addALS" )
    public String addALS(@PathVariable(value = "id") Long projectId, Model model) {
        ProjectDTO projectDTO = projectService.addNewALSandSaveProject(projectId);
        model.addAttribute("project",projectDTO);

        return "redirect:/projects/"+projectId;
    }
    @GetMapping("/{id}/alss/{alsId}/delete" )
    public String deleteALS(@PathVariable(value = "id") Long id,
                            @PathVariable(value = "alsId") Long alsId,
                            Model model) {
        model.addAttribute("project", projectService.deleteALSandSaveProject(id, alsId));
        return "redirect:/projects/"+id;
    }
    @GetMapping("/{id}")
    private String editProject(@PathVariable(value = "id") Long id, Model model) {
        Optional<ProjectDTO> projectOptional = projectService.findById(id);
        ProjectDTO project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}

        model.addAttribute("project", project);

        model.addAttribute("colorsList", colorsList);
        model.addAttribute("positionLCList", positionLCList);
        model.addAttribute("paymentList", paymentList);
        model.addAttribute("displayList", displayList);
        model.addAttribute("barReaderList", barReaderList);
        model.addAttribute("typeList", typeLbList);
        return "projects/project";
    }
    @PostMapping("/{projectId}/alss/{alsId}/save")
    public String saveALSatProject(
            @PathVariable(value = "projectId") Long projectId,
            @PathVariable(value = "alsId") Long alsId,
            @ModelAttribute("als") ALSDTO als,
            Model model) {
        Optional<ProjectDTO> projectOptional =projectService.findById(projectId);
        ProjectDTO project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
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
            logger.warn(String.valueOf(errorALSList));
            logger.warn(String.valueOf(errorLCList));
            logger.warn(String.valueOf(errorLBLists));
            model.addAttribute("ALSErrors", errorALSList);
            model.addAttribute("LCErrors", errorLCList);
            model.addAttribute("LBErrors", errorLBLists);
            model.addAttribute("als", als);
            model.addAttribute("colorsList", colorsList);
            model.addAttribute("positionLCList", positionLCList);
            model.addAttribute("paymentList", paymentList);
            model.addAttribute("typeList", typeLbList);
            model.addAttribute("displayList", displayList);
            model.addAttribute("barReaderList", barReaderList);
            return "projects/project_als";
        }
        return "redirect:/projects/"+projectId+"/alss/"+als.getId();
    }

    @GetMapping("/{projectId}/alss/{alsId}" )
    public String editALSatProject(@PathVariable(value = "projectId") Long projectId,
                                   @PathVariable(value = "alsId") Long alsId,
                                   Model model) {

        Optional<ProjectDTO> projectOptional =projectService.findById(projectId);
        ProjectDTO project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        model.addAttribute("project", project);

        Optional<ALSDTO> alsOptional = alsService.findById(alsId);
        ALSDTO als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        model.addAttribute("als", als);
        model.addAttribute("typeList", typeLbList);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("positionLCList", positionLCList);
        model.addAttribute("paymentList", paymentList);
        model.addAttribute("displayList", displayList);
        model.addAttribute("barReaderList", barReaderList);
        return "projects/project_als";
    }
    @GetMapping("/{projectId}/alss/{alsId}/lcs/{lcId}")
    private String editLCatProject(@PathVariable(value = "projectId") Long projectId,
                                   @PathVariable(value = "alsId") Long alsId,
                                   @PathVariable(value = "lcId") Long lcId,
                                   Model model) {
        Optional<ProjectDTO> projectOptional = projectService.findById(projectId);
        ProjectDTO project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        Optional<ALSDTO> alsOptional = alsService.findById(alsId);
        ALSDTO als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        Optional<LCDTO> lcOptional = lcService.findById(lcId);
        LCDTO lc = null;
        if (lcOptional.isPresent()) {lc = lcOptional.get();}
        model.addAttribute("project", project);
        model.addAttribute("als", als);
        model.addAttribute("lc", lc);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("paymentList", paymentList);
        model.addAttribute("displayList", displayList);
        model.addAttribute("barReaderList", barReaderList);
        return "projects/project_lc";
    }
    @PostMapping("/{projectId}/alss/{alsId}/lcs/{lcId}/save")
    public String saveLCatProject(
            @PathVariable(value = "projectId") Long projectId,
            @PathVariable(value = "alsId") Long alsId,
            @PathVariable(value = "lcId") Long lcId,
            @ModelAttribute("lc") LCDTO lc,
            Model model) {
        Optional<ProjectDTO> projectOptional = projectService.findById(projectId);
        ProjectDTO project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        Optional<ALSDTO> alsOptional = alsService.findById(alsId);
        ALSDTO als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        List<String> errorList =SizeValidator.getErrorValidateLCSizesList(lc);
        if (errorList.isEmpty()) {
            als=projectService.replaceLCandSaveProject(project,als,alsId,lc);
        }
        else {
            logger.warn(errorList.toString());
            model.addAttribute("lc", lc);
            model.addAttribute("errors", errorList);
            model.addAttribute("colorsList", colorsList);
            model.addAttribute("paymentList", paymentList);
            model.addAttribute("displayList", displayList);
            model.addAttribute("barReaderList", barReaderList);
            return "projects/project_lc";
        }
        model.addAttribute("project", project);
        model.addAttribute("als", als);

        return "redirect:/projects/"+projectId+"/alss/"+ als.getId()+"/lcs/"+als.getLC().getId();
    }
    @GetMapping("/{projectId}/alss/{alsId}/addLB" )
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
        Optional<ProjectDTO> projectOptional = projectService.findById(projectId);
        ProjectDTO project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        Optional<ALSDTO> alsOptional = alsService.findById(alsId);
        ALSDTO als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        Optional<LBDTO> lbOptional = lbService.findById(lbId);
        LBDTO lb = null;
        if (lbOptional.isPresent()) {lb = lbOptional.get();}

        model.addAttribute("project", project);
        model.addAttribute("als", als);
        model.addAttribute("lb", lb);
        model.addAttribute("typeLbList", typeLbList);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("directionDoorOpeningList", directionDoorOpeningList);
        return "projects/project_lb";
    }

    @PostMapping("/{projectId}/alss/{alsId}/lbs/{lbId}/save")
    public String saveLBatProject(@PathVariable(value = "projectId") Long projectId,
                                  @PathVariable(value = "alsId") Long alsId,
                                  @PathVariable(value = "lbId") Long lbId,
                                  @ModelAttribute("lb") LBDTO lb,
                                  Model model) {
        Optional<ProjectDTO> projectOptional = projectService.findById(projectId);
        ProjectDTO project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        model.addAttribute("project", project);
        List<Object> ALSlbIdList= null;
        int newLbId;
        Optional<ALSDTO> alsOptional = alsService.findById(alsId);
        ALSDTO als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);
        model.addAttribute("als", als);
        List<String> errorList= SizeValidator.getErrorValidateLBSizesList(lb);
        if (errorList.isEmpty()) {
             ALSlbIdList = projectService.saveLBatProject(projectId,alsId,lbId,lb);
             als=(ALSDTO) ALSlbIdList.get(0);
             newLbId = (int) ALSlbIdList.get(1);

        } else {
            logger.warn(errorList.toString());
            model.addAttribute("errors",errorList);
            model.addAttribute("lb", lb);
            model.addAttribute("typeLbList", typeLbList);
            model.addAttribute("colorsList", colorsList);
            model.addAttribute("directionDoorOpeningList", directionDoorOpeningList);
            return "projects/project_lb";
        }

        return "redirect:/projects/"+projectId+"/alss/"+als.getId()+"/lbs/"+newLbId;
    }
    @GetMapping("/{id}/savetoexcel")
    private ResponseEntity<Resource> saveProjectToExcel(@PathVariable(value = "id") Long id) {
        Optional<ProjectDTO> projectOptional = projectService.findById(id);
        ProjectDTO project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        String filename = project.getName()+".xlsx";
        InputStreamResource file = new InputStreamResource(projectService.exportToExcel(project));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

}
