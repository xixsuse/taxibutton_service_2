package tb.cost;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tb.domain.Partner;
import tb.domain.maparea.Point;
import tb.service.BrandingService;

@Service
public class CostRequest {
	private static final Logger logger = LoggerFactory.getLogger(CostRequest.class);

	@Autowired
	private PartnersApiKeeper partnersApiKeeper;
	@Autowired
	private BrandingService brandingService;

	@Transactional
	public DeferredResult<String> getCostAsync(String brandName, Point source, List<Point> destinations,
			String carClass,
			String carBasket, Date bookDate, List<String> adds) {
		String requestStr = createRequestBody(source, destinations, carClass, carBasket, bookDate, adds);
		logger.debug("COST JSON: " + requestStr);

		// cost http requests
		DeferredResult<String> dr = new DeferredResult<>();
		Partner majorPartner = brandingService.getMajorPartner(brandName);
		if (majorPartner != null) {
			PartnerApi api = partnersApiKeeper.getPartnerApi(majorPartner);

			Call<String> call = api.cost(requestStr);
			call.enqueue(new Callback<String>() {

				@Override
				public void onResponse(Call<String> call, Response<String> response) {
					String responseString = response.body();
					logger.info("COST SUCCESS: " + responseString);
					JSONObject responseJson = (JSONObject) new JSONTokener(responseString).nextValue();
					CostResponse cr = convertCostResponse(responseJson, majorPartner.getName());
					JSONObject costJson = new JSONObject();
					costJson.put("sum", cr.getPrice());
					costJson.put("km", cr.getKm());
					costJson.put("min", cr.getMin());
					costJson.put("partner", cr.getPartnerName());

					dr.setResult(costJson.toString());
				}

				@Override
				public void onFailure(Call<String> call, Throwable t) {
					logger.warn("COST FAILURE: ResponseCode=" + t.getMessage() + " Partnername="
							+ majorPartner.getName());
					dr.setErrorResult(t);
				}
			});
		}

		return dr;
	}

	private String createRequestBody(Point source, List<Point> destinations, String carClass, String carBasket,
			Date bookDate, List<String> adds) {
		JSONObject requestJson = new JSONObject();
		requestJson.put("RoutingServiceName", "YandexMapsService");
		requestJson.put("TaxiServiceId", "taxirf");
		requestJson.put("BookingTime", getBookDate(bookDate));
		requestJson.put("CarClass", carClass);
		if (carBasket != null && !carBasket.isEmpty()) {
			requestJson.put("CarBasket", carBasket);
		}
		requestJson.put("Source", getJsonPoint(source));
		if (destinations != null) {
			requestJson.put("Destinations", getJsonPoints(destinations));
		} else {
			List<Point> fakeDestinations = new ArrayList<>();
			fakeDestinations.add(source);
			requestJson.put("Destinations", getJsonPoints(fakeDestinations));
		}
		if (adds != null) {
			requestJson.put("AdditionalServices", getAdds(adds));
		}
		return requestJson.toString();
	}

	private JSONArray getAdds(List<String> adds) {
		JSONArray array = new JSONArray();
		for (String add : adds) {
			array.put(add);
		}
		return array;
	}

	private JSONArray getJsonPoints(List<Point> points) {
		JSONArray array = new JSONArray();
		for (Point point : points) {
			array.put(getJsonPoint(point));
		}
		return array;
	}

	private JSONObject getJsonPoint(Point point) {
		JSONObject pointJson = new JSONObject();
		pointJson.put("Lat", point.getLatitude());
		pointJson.put("Lon", point.getLongitude());
		return pointJson;
	}

	private String getBookDate(Date bookDate) {
		// "2008-12-28T00:00:00",
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		return dateFormat.format(bookDate);
	}

	private CostResponse convertCostResponse(JSONObject responseJson, String parnetName) {

		CostResponse cr = new CostResponse();
		cr.setPrice(responseJson.getDouble("TotalPrice"));

		JSONObject responseNestedJson = responseJson.getJSONObject("Calculated");
		cr.setKm(responseNestedJson.getDouble("Km"));
		cr.setMin(responseNestedJson.getDouble("Min"));
		cr.setPartnerName(parnetName);

		return cr;
	}
}
