// package com.ma.dlp.StatsController;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;

// import com.ma.dlp.RestService.DashBoardService;
// import com.ma.dlp.StatDTO.DashboardStatsDTO;

// @Controller
// public class DashboardViewController {

//     @Autowired
//     private DashBoardService dashboardService;

//     public DashboardViewController(DashBoardService dashboardService) {
//         this.dashboardService = dashboardService;
//     }

//     @GetMapping("/dashboard")
//     public String dashboard(Model model) {

//         DashboardStatsDTO stats = dashboardService.getStats();
//         model.addAttribute("stats", stats);

//         return "dashboard"; // dashboard.html
//     }
// }