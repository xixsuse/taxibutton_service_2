package tb.dao;

import java.util.List;

import tb.domain.Brand;
import tb.domain.BrandService;
import tb.domain.Partner;

public interface IBrandDao {
	Brand get(String codeName);

	List<BrandService> getBrandServices(Partner partner);

	List<Brand> getAll();

	Brand get(Long brandId);

	void saveBrand(Brand brand);

	void deleteBrandServices(Brand brand);
}
