package com.project.ecommerce.Service;

import com.project.ecommerce.Model.Home;
import com.project.ecommerce.Model.HomeCategory;

import java.util.List;

public interface HomeService {

    public Home createHomePageData(List<HomeCategory> allCategories);

}
