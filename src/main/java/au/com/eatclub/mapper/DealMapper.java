package au.com.eatclub.mapper;

import au.com.eatclub.model.ActiveDeal;
import au.com.eatclub.model.Deal;
import au.com.eatclub.model.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DealMapper {
    DealMapper INSTANCE = Mappers.getMapper(DealMapper.class);

    @Mapping(target = "restaurantObjectId", source = "restaurant.objectId")
    @Mapping(target = "restaurantName", source = "restaurant.name")
    @Mapping(target = "restaurantAddress1", source = "restaurant.address1")
    @Mapping(target = "restaurantSuburb", source = "restaurant.suburb")
    @Mapping(target = "restaurantOpen", source = "restaurant.open")
    @Mapping(target = "restaurantClose", source = "restaurant.close")
    @Mapping(target = "dealObjectId", source = "deal.objectId")
    @Mapping(target = "discount", source = "deal.discount")
    @Mapping(target = "dineIn", source = "deal.dineIn")
    @Mapping(target = "lightning", source = "deal.lightning")
    @Mapping(target = "qtyLeft", source = "deal.qtyLeft")
    ActiveDeal mapActiveDeal(Restaurant restaurant, Deal deal);
}
